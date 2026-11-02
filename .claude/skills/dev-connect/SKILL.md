---
name: dev-connect
description: dev 환경의 RDS MySQL이나 Valkey(캐시)에 SSM 포트 포워딩으로 접속해 조회한다(로컬 포트 → 실행 중인 앱 ECS task → 대상). 사용자가 dev DB·캐시 접속, 개발 DB 데이터나 테이블 확인, dev Flyway 이력 확인, dev Redis·Valkey 키 확인, DB 클라이언트로 dev에 붙기를 요청할 때 사용.
---

RDS와 Valkey는 인터넷에 열려 있지 않다. 실행 중인 앱 task의 SSM agent를 거쳐 로컬 포트를 대상으로 이어 붙인다. 열린 포트나 bastion 없이 IAM 권한으로만 들어가기 때문이다.

| 대상 | 원격 주소 | 로컬 포트 | 인증 |
|---|---|---|---|
| DB | RDS `deepple-dev` 엔드포인트 `:3306` | `13306` | 사용자 `deepple`, DB `deepple`, 비밀번호는 `deepple-dev/app` secret의 `MYSQL_PASSWORD` |
| 캐시 | `valkey.deepple-dev.local:6379` | `16379` | 없음 |

## 준비

- AWS 자격 증명: `aws sts get-caller-identity`의 계정이 `339713092351`인지 확인한다.
- Session Manager 플러그인: `session-manager-plugin --version`이 실패하면 사용자에게 `brew install --cask session-manager-plugin`을 직접 실행해 달라고 한다. 설치에 관리자 비밀번호가 필요하다.
- 앱 task가 떠 있어야 한다. 야간 중지 시간(기본 02~08시 KST, `infra/terraform/envs/dev/variables.tf`의 `nightly_shutdown`)에는 task가 없어 접속할 수 없다. Valkey는 꺼지지 않지만 거쳐 갈 앱 task가 없다.

## 절차

1. task와 컨테이너 runtime ID를 찾는다.
   ```bash
   TASK=$(aws ecs list-tasks --cluster deepple-dev --service-name deepple-dev-api --query 'taskArns[0]' --output text | cut -d/ -f3)
   RUNTIME=$(aws ecs describe-tasks --cluster deepple-dev --tasks $TASK --query "tasks[0].containers[?name=='app'].runtimeId" --output text)
   ```
2. 포트 포워딩을 연다. 세션이 계속 떠 있어야 하므로 백그라운드로 실행하고, 출력에 `Waiting for connections...`가 나오면 준비된 것이다. DB면 `HOST`를 RDS 엔드포인트, `REMOTE=3306`, `LOCAL=13306`으로, 캐시면 `HOST=valkey.deepple-dev.local`, `REMOTE=6379`, `LOCAL=16379`로 둔다.
   ```bash
   HOST=$(aws rds describe-db-instances --db-instance-identifier deepple-dev --query 'DBInstances[0].Endpoint.Address' --output text)
   aws ssm start-session --target "ecs:deepple-dev_${TASK}_${RUNTIME}" \
     --document-name AWS-StartPortForwardingSessionToRemoteHost \
     --parameters "{\"host\":[\"$HOST\"],\"portNumber\":[\"$REMOTE\"],\"localPortNumber\":[\"$LOCAL\"]}"
   ```
3. 접속한다. 사용자가 직접 붙을 때는 위 표의 로컬 포트와 인증 정보를 클라이언트(IntelliJ Database, DataGrip, RedisInsight 등)에 넣으라고 안내한다. DB 비밀번호는 사용자가 직접 조회한다. 에이전트가 조회할 때는 로컬에 클라이언트가 없을 수 있어 Docker 이미지를 쓴다.
   - DB: 비밀번호는 화면에 출력하지 않고 환경 변수로만 넘긴다.
     ```bash
     MYSQL_PWD=$(aws secretsmanager get-secret-value --secret-id deepple-dev/app --query SecretString --output text | jq -r .MYSQL_PASSWORD) \
       docker run --rm -e MYSQL_PWD mysql:8.4 mysql -h host.docker.internal -P 13306 -u deepple deepple -e "<쿼리>"
     ```
   - 캐시: 값에 refresh token 같은 인증 정보가 들어 있으니 키와 값을 그대로 출력하지 말고 필요한 만큼만 보거나 가린다.
     ```bash
     docker run --rm valkey/valkey:8.1 valkey-cli -h host.docker.internal -p 16379 <명령>
     ```
4. 데이터를 바꾸는 작업(INSERT·UPDATE·DELETE·DDL, `DEL`·`FLUSHALL` 등)은 실행 전에 명령을 보여 주고 확인받는다. dev라도 다른 사람이 테스트 중인 데이터일 수 있다. 스키마 변경은 Flyway 마이그레이션으로만 한다.
5. 끝나면 포트 포워딩 세션을 종료한다. 열어 둔 세션은 앱 task의 SSM 연결을 계속 잡고 있다.

## 문제 해결

- `SessionManagerPlugin is not found`: 플러그인이 없거나 설치가 끝나지 않았다(.pkg 설치에서 비밀번호 단계가 끝나야 한다).
- `TargetNotConnected` 또는 task가 `None`: 앱 task가 없다. 야간 중지 시간인지, 서비스가 실패 중인지 `aws ecs describe-services --cluster deepple-dev --services deepple-dev-api`로 확인한다.
- 로컬 포트 사용 중: 이전 세션이 남아 있다. 종료하거나 `LOCAL`을 바꾼다.
