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
    └── prod/
```

`envs/prod`는 작성 중이다.

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

구성은 ALB(`dev-api.deepple.co.kr`) → ECS Fargate Spot 앱 task 1개 → RDS MySQL이다. Valkey는 별도 ECS service(`valkey.deepple-dev.local`)로 띄운다. 앱의 지표·trace·로그는 Alloy ECS service(`alloy.deepple-dev.local`)를 거쳐 Grafana Cloud로 보낸다. 콘솔 로그는 CloudWatch(`/ecs/deepple-dev`)에 14일 보관한다. NAT 없이 퍼블릭 서브넷에서 실행하고 RDS는 비공개 서브넷에 둔다.

- 매일 02~08시(KST)에 앱·Alloy와 RDS를 끈다. RDS·Alloy는 08:00, 앱은 08:15에 켜진다. Valkey는 끄지 않는다.
  - 시각과 사용 여부는 `variables.tf`의 `nightly_shutdown` 기본값을 바꿔 apply한다.
  - 한 번만 바꿀 때는 `terraform apply -var 'nightly_shutdown={enabled=false,stop_hour=2,start_hour=8}'`처럼 넘긴다. 다음 apply 때 기본값으로 돌아간다.
  - 꺼져 있는 시간에 사용을 끄면 앱·Alloy와 RDS는 꺼진 채로 남는다. 직접 켜야 한다.
- 이미지 버킷 `deepple-dev-storage`는 읽기 공개다.
- Valkey task가 바뀌면(Spot 회수, 장애) 데이터(인증번호, refresh token)가 사라진다.
- DB·캐시 접속: RDS와 Valkey는 비공개라 앱 task를 거쳐 SSM 포트 포워딩으로 붙는다. 절차는 `/dev-connect` 스킬(`.claude/skills/dev-connect/SKILL.md`)에 있다.

## apply 전 준비물

Terraform은 secret을 만들기만 하고 값은 넣지 않는다. 값이 코드와 state에 남지 않게 하려는 것이다. 처음 apply할 때는 secret만 먼저 만들고(`-target`) 값을 넣은 뒤 나머지를 apply한다. 값이 없으면 ECS task가 시작하지 못한다.

### Secret 값

| Secret | 내용 | 관리 |
|---|---|---|
| `deepple-dev/app` | `JWT_SECRET`, `MYSQL_PASSWORD` | Terraform이 랜덤 생성 (state에 남지 않음) |
| `deepple-dev/external` | App Store 앱 ID, Bizgo 키, Sentry DSN | 직접 넣는다 |
| `deepple-dev/grafana-cloud` | Alloy가 Grafana Cloud로 보낼 때 쓰는 Access Policy 토큰 | 직접 넣는다 |
| `ecr-pullthroughcache/docker-hub` (shared) | ECR pull through cache용 Docker Hub access token | 직접 넣는다 |

`deepple-dev/external`은 키 이름이 `secrets.tf`의 `external_secret_keys`와 같은 JSON이다. 로컬 `.env`에서 만들어 넣을 수 있다.

```bash
set -a; source .env; set +a
jq -n 'env | {APP_STORE_APP_APPLE_ID,
  BIZGO_API_URL, BIZGO_API_KEY, BIZGO_FROM_PHONE_NUMBER}' \
  | aws secretsmanager put-secret-value --secret-id deepple-dev/external --secret-string file:///dev/stdin
```

Discord 웹훅 URL은 채널에서 복사한 뒤 넣는다. secret 이름은 아래 알림 표에 있다.

```bash
aws secretsmanager put-secret-value --secret-id <웹훅 secret> --secret-string "$(pbpaste)"
```

Grafana Cloud 토큰은 Grafana Cloud Portal → 스택 → OpenTelemetry 타일에서 만든다. 권한은 `metrics:write`, `logs:write`, `traces:write`만 준다. 엔드포인트와 인스턴스 ID는 같은 타일에 나오며 `variables.tf`의 `grafana_cloud_otlp_endpoint`, `grafana_cloud_instance_id`와 같아야 한다.

```bash
aws secretsmanager put-secret-value --secret-id deepple-dev/grafana-cloud --secret-string "$(pbpaste)"
```

Docker Hub 토큰은 Docker Hub → Account settings → Personal access tokens에서 읽기 전용(Public Repo Read-only)으로 만든다.

```bash
jq -n --arg username <Docker Hub 사용자 이름> --arg accessToken "$(pbpaste)" '{username: $username, accessToken: $accessToken}' \
  | aws secretsmanager put-secret-value --secret-id ecr-pullthroughcache/docker-hub --secret-string file:///dev/stdin
```

### Google (Firebase, Google Play)

키 파일 없이 Workload Identity Federation을 쓴다. 앱 역할로 Google에 인증해 서비스 계정을 impersonate한다. 환경마다 Google Cloud 프로젝트를 나눈다.

| 환경 | 프로젝트 | 풀·공급자 | Firebase | Google Play |
|---|---|---|---|---|
| dev | `atwoz-d63ef` | `aws-deepple` / `deepple-dev` | `firebase-adminsdk-ox847` | `deepple-dev-play-verifier` |
| prod | `deepple-c5416` | | `firebase-adminsdk-fbsvc` | `deepple-play-verifier` |

dev 구성 절차는 아래와 같다. prod도 같은 방식으로 `deepple-c5416`에 만든다.

1. API를 켜고 풀과 AWS 공급자를 만든다. 공급자는 dev 앱 역할(`deepple-dev-app`)만 허용한다.
   ```bash
   gcloud services enable sts.googleapis.com iamcredentials.googleapis.com androidpublisher.googleapis.com --project=atwoz-d63ef
   gcloud iam workload-identity-pools create aws-deepple --project=atwoz-d63ef --location=global --display-name="AWS deepple"
   gcloud iam workload-identity-pools providers create-aws deepple-dev --project=atwoz-d63ef --location=global \
     --workload-identity-pool=aws-deepple --account-id=339713092351 \
     --attribute-condition="attribute.aws_role == 'arn:aws:sts::339713092351:assumed-role/deepple-dev-app'"
   ```
2. Google Play 결제 검증용 서비스 계정을 만들고, Play Console → 사용자 및 권한에서 이 계정을 초대해 앱 권한으로 재무 데이터 보기와 주문·구독 관리를 준다.
   ```bash
   gcloud iam service-accounts create deepple-dev-play-verifier --project=atwoz-d63ef --display-name="dev Play 결제 검증"
   ```
3. 앱 역할에 두 서비스 계정의 impersonate 권한을 준다. `<번호>`는 `gcloud projects describe atwoz-d63ef --format='value(projectNumber)'`로 확인한다.
   ```bash
   MEMBER="principalSet://iam.googleapis.com/projects/<번호>/locations/global/workloadIdentityPools/aws-deepple/attribute.aws_role/arn:aws:sts::339713092351:assumed-role/deepple-dev-app"
   for sa in firebase-adminsdk-ox847 deepple-dev-play-verifier; do
     gcloud iam service-accounts add-iam-policy-binding $sa@atwoz-d63ef.iam.gserviceaccount.com \
       --project=atwoz-d63ef --role=roles/iam.workloadIdentityUser --member="$MEMBER"
   done
   ```
4. `variables.tf`의 `gcp_workload_identity_provider`, `firebase_service_account`, `google_play_service_account`가 위 값과 같은지 확인한다.

## 알림

알림은 `modules/alerts`(SNS 토픽 → Lambda → Discord 웹훅)로 환경마다 따로 보낸다.

| 환경 | 채널 | 내용 | 웹훅 secret |
|---|---|---|---|
| shared | `#alerts-account` | 예산 초과, 루트 계정 사용, dev 알림 전송 실패 | `deepple/alerts/discord-webhook` |
| dev | `#alerts-dev` | 서버 오류(5xx), 메모리, DB 저장 공간, ECS task 시작·배치 실패, 배포 실패·성공 | `deepple-dev/alerts/discord-webhook` |

- 멘션 역할은 각 환경 `variables.tf`의 `discord_mention_role_id`로 정한다.
- 메시지 형식은 Datadog을 따른다. 제목은 `[Triggered on dev] High memory usage`처럼 상태·환경·영문 제목이고(복구는 `Recovered`, 배포는 `Deployed`), 본문은 What's happening? / Possible causes / How to troubleshoot? 칸에 한글로 쓴다.
- CloudWatch alarm의 `alarm_description`은 JSON(`title`, `what`, `why`, `how`, `unit`)이다. `what`의 `{value}`는 측정값, `{condition}`은 `> 90%` 같은 조건으로 채워진다. `why`, `how`는 목록이면 불릿으로 표시된다. 환경 이름은 모듈의 `environment`로 붙는다.
- 환경 알림 Lambda가 오류를 내면 `#alerts-account`로 알린다. 고장 난 Lambda는 자기 고장을 알릴 수 없기 때문이다.

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
