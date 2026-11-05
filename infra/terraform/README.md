# Terraform

딥플의 AWS 인프라 코드이다. 계정 하나(`339713092351`), 리전 `ap-northeast-2`에서 환경(dev, prod)을 나눠 관리한다.

## 구조

```
infra/terraform/
├── bootstrap/   # state 저장용 S3 버킷
├── modules/     # 직접 작성한 재사용 모듈 (alerts)
└── envs/
    ├── shared/  # 계정 공통 (ECR, GitHub OIDC, Route 53, 알림, CloudTrail)
    ├── dev/     # 개발 서버 (dev-api.deepple.co.kr)
    ├── grafana/ # Grafana Cloud 알람·대시보드 (dev·prod 공용 스택 deepple)
    └── prod/    # 운영 서버 (api.deepple.co.kr, 아직 apply 안 함)
```

## 시작하기

1. Terraform 1.11 이상을 설치한다.
2. AWS 자격 증명을 설정하고 계정을 확인한다.
   ```bash
   aws sts get-caller-identity   # Account가 339713092351인지 확인
   ```
3. 작업할 환경 디렉터리에서 초기화한다. state는 S3에서 자동으로 가져온다.
   ```bash
   cd infra/terraform/envs/dev
   terraform init
   ```

`bootstrap/`은 실행하지 않는다. state 버킷은 이미 만들어져 있고, 다시 실행하면 같은 버킷을 새로 만들려다 실패한다.

## 작업 흐름

```bash
terraform fmt -recursive   # 포맷
terraform validate         # 문법 검사
terraform plan             # 변경 미리보기 (AWS는 바뀌지 않음)
terraform apply            # 실제 적용
```

- 코드 변경은 다른 코드와 같이 이슈 → 브랜치 → PR로 올린다.
- `apply` 전에 plan 결과의 생성·변경·삭제 수를 반드시 확인한다. 특히 삭제(`destroy`)와 교체(`replace`)가 있으면 이유를 확인한다.
- 환경은 `shared` → `dev` → `prod` 순서로 적용한다. dev에서 먼저 확인하고 prod에 적용한다.

## 환경

### dev

구성은 API Gateway HTTP API(`dev-api.deepple.co.kr`) → VPC Link → Cloud Map → ECS Fargate Spot 앱 task 1개(ARM) → RDS MySQL이다. Valkey는 별도 ECS service(`valkey.deepple-dev.local`)로 띄운다. 앱의 지표·trace·로그는 Alloy ECS service(`alloy.deepple-dev.local`)를 거쳐 Grafana Cloud로 보낸다. 콘솔 로그는 CloudWatch(`/ecs/deepple-dev`)에 14일 보관한다. NAT 없이 퍼블릭 서브넷에서 실행하고 RDS는 비공개 서브넷에 둔다.

- 매일 02~08시(KST)에 앱·Alloy와 RDS를 끈다. RDS·Alloy는 08:00, 앱은 08:15에 켜진다. Valkey는 끄지 않는다.
  - 시각과 사용 여부는 `variables.tf`의 `nightly_shutdown` 기본값을 바꿔 apply한다.
  - 시각이나 사용 여부를 바꾸면 `envs/grafana`의 `environments.dev.quiet_hours`도 같이 바꾼다. 앱이 꺼져 있는 동안 Grafana 알림을 보내지 않는 시간대다.
  - 한 번만 바꿀 때는 `terraform apply -var 'nightly_shutdown={enabled=false,stop_hour=2,start_hour=8}'`처럼 넘긴다. 다음 apply 때 기본값으로 돌아간다.
  - 꺼져 있는 시간에 사용을 끄면 앱·Alloy와 RDS는 꺼진 채로 남는다. 직접 켜야 한다.
- 이미지 버킷 `deepple-dev-storage`는 읽기 공개다.
- Valkey task가 바뀌면(Spot 회수, 장애) 데이터(인증번호, refresh token)가 사라진다.
- DB·캐시 접속: RDS와 Valkey는 비공개라 앱 task를 거쳐 SSM 포트 포워딩으로 붙는다. 절차는 `/dev-connect` 스킬(`.claude/skills/dev-connect/SKILL.md`)에 있다.

### prod

코드만 있고 아직 apply하지 않았다. 출시 준비 때 띄운다.

- 배포: `v1.2.3` 같은 태그를 push하면 `Deploy prod` 워크플로가 GitHub `prod` 환경 승인을 기다린다. 승인하면 dev에서 빌드한 그 커밋의 이미지를 확인(provenance)하고 버전 태그를 붙여 배포한다. 다시 빌드하지 않는다.
- 이미지가 없다고 실패하면: 앱 코드가 바뀌지 않은 커밋이라 dev 빌드가 돌지 않은 것이다. `Deploy` 워크플로를 그 커밋에서 수동 실행한 뒤 다시 실행한다.
- 되돌리기: Actions → `Deploy prod` → Run workflow에서 "Use workflow from"을 이전 버전 태그로 골라 실행한다. main에서 실행하면 아무것도 하지 않는다.
- ECR은 `v` 태그 이미지를 최근 20개까지 따로 보관한다.

## Secret

Terraform은 secret을 만들기만 하고, 직접 넣는 값은 `aws secretsmanager put-secret-value`로 넣는다. 값이 코드와 state에 남지 않게 하려는 것이다.

| Secret | 내용 | 관리 |
|---|---|---|
| `deepple-dev/app` | `MYSQL_PASSWORD` | Terraform이 랜덤 생성 (state에 남지 않음) |
| `deepple-dev/jwt` | `JWT_PRIVATE_KEY` (RSA 2048 PKCS#8 PEM, 토큰 서명용) | Terraform이 랜덤 생성 (state에 남지 않음). 교체는 `secret_string_wo_version`을 올린다 |
| `deepple-dev/external` | App Store 앱 ID, Bizgo 키, Sentry DSN (키 이름은 `secrets.tf`의 `external_secret_keys`) | 직접 넣는다 |
| `deepple-dev/grafana-cloud` | Alloy가 Grafana Cloud로 보낼 때 쓰는 Access Policy 토큰 | 직접 넣는다 |
| `deepple-dev/alerts/discord-webhook` | `#alerts-dev` 웹훅 URL | 직접 넣는다 |
| `deepple/alerts/discord-webhook` (shared) | `#alerts-account` 웹훅 URL | 직접 넣는다 |
| `ecr-pullthroughcache/docker-hub` (shared) | ECR pull through cache용 Docker Hub access token | 직접 넣는다 |
| `deepple/grafana-terraform` (shared) | `envs/grafana`가 쓰는 Grafana 스택 서비스 계정 토큰 | 직접 넣는다 |

## Google (Firebase, Google Play)

키 파일 없이 Workload Identity Federation으로 앱 역할이 서비스 계정을 impersonate한다. 환경마다 Google Cloud 프로젝트를 나눈다.

| 환경 | 프로젝트 | 풀·공급자 | Firebase | Google Play |
|---|---|---|---|---|
| dev | `atwoz-d63ef` | `aws-deepple` / `deepple-dev` | `firebase-adminsdk-ox847` | `deepple-dev-play-verifier` |
| prod | `deepple-c5416` | | `firebase-adminsdk-fbsvc` | `deepple-play-verifier` |

## 알림

AWS 자원 알림은 `modules/alerts`(SNS 토픽 → Lambda → Discord 웹훅)로, 앱 지표 알림은 Grafana(`envs/grafana`)에서 같은 채널로 보낸다.

| 환경 | 채널 |
|---|---|
| shared | `#alerts-account` |
| dev | `#alerts-dev` |
| prod | `#alerts-prod` |

- 멘션 역할은 각 환경 `variables.tf`의 `discord_mention_role_id`로 정한다.
- prod Grafana 알림 규칙은 prod를 띄우기 전까지 멈춰 둔다(`envs/grafana`의 `environments.prod.paused`). 띄운 뒤 false로 바꿔 apply한다.
- `envs/grafana`는 실행 전에 Grafana 토큰을 환경 변수로 넘긴다.
  ```bash
  export GRAFANA_AUTH=$(aws secretsmanager get-secret-value --secret-id deepple/grafana-terraform --query SecretString --output text)
  ```

## state

- 저장 위치: S3 버킷 `deepple-terraform-state-339713092351`
- 환경마다 key를 나눈다(예: `envs/dev/terraform.tfstate`).
- S3 자체 잠금(`use_lockfile = true`)을 써서 같은 환경에 동시에 apply할 수 없다.
- 버전 관리가 켜져 있어 state가 깨지면 S3 콘솔에서 이전 버전으로 되돌릴 수 있다. 이전 버전은 90일 뒤 삭제되고, 최근 10개는 기간과 관계없이 남는다.

## 규칙

- `.terraform.lock.hcl`은 커밋한다. provider를 추가·업그레이드하면 CI(Linux)용 해시도 함께 기록한다.
  ```bash
  terraform providers lock -platform=darwin_arm64 -platform=linux_amd64
  ```
- Secret 값은 코드나 tfvars에 넣지 않는다. Secrets Manager를 쓴다.
- IAM 역할·정책의 `description`은 영문으로 쓴다. 한글은 apply 때 AWS가 거부한다.
- 커뮤니티 모듈은 버전을 고정한다(VPC는 `terraform-aws-modules/vpc/aws`).
- AWS 콘솔에서 Terraform이 관리하는 리소스를 직접 수정하지 않는다. 다음 apply 때 코드 상태로 되돌아간다.
- 모든 리소스에는 provider의 `default_tags`로 `Project`, `Stack`, `ManagedBy` 태그가 붙는다.
