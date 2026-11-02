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

## 알림

알림은 `modules/alerts`(SNS 토픽 → Lambda → Discord 웹훅)로 환경마다 따로 보낸다.

| 환경 | 채널 | 내용 | 웹훅 secret |
|---|---|---|---|
| shared | `#alerts-account` | 예산 초과, 루트 계정 사용 | `deepple/alerts/discord-webhook` |
| dev | `#alerts-dev` | dev 서버 오류·메모리, DB 저장 공간, ECS task 시작 실패·배포 실패 | `deepple-dev/alerts/discord-webhook` |

- Terraform은 빈 secret만 만들고 웹훅 URL은 직접 넣는다. Discord에서 URL을 복사한 뒤 실행한다.
  ```bash
  aws secretsmanager put-secret-value --secret-id <웹훅 secret> --secret-string "$(pbpaste)"
  ```
- 멘션 역할은 각 환경 `variables.tf`의 `discord_mention_role_id`로 정한다.
- CloudWatch alarm의 `alarm_description`은 첫 줄이 Discord 메시지 제목, 나머지가 본문이 된다.

## dev 환경

구성은 ALB(`dev-api.deepple.co.kr`) → ECS Fargate Spot 앱 task 1개 → RDS MySQL이다. Valkey는 별도 ECS service(`valkey.deepple-dev.local`)로 띄운다. NAT 없이 퍼블릭 서브넷에서 실행하고 RDS는 비공개 서브넷에 둔다.

- 매일 02~08시(KST)에 앱과 RDS를 끈다. RDS는 08:00, 앱은 08:15에 켜진다. Valkey는 끄지 않는다.
  - 시각과 사용 여부는 `variables.tf`의 `nightly_shutdown` 기본값을 바꿔 apply한다.
  - 한 번만 바꿀 때는 `terraform apply -var 'nightly_shutdown={enabled=false,stop_hour=2,start_hour=8}'`처럼 넘긴다. 다음 apply 때 기본값으로 돌아간다.
  - 꺼져 있는 시간에 사용을 끄면 앱과 RDS는 꺼진 채로 남는다. 직접 켜야 한다.
- 이미지 버킷 `deepple-dev-storage`는 읽기 공개다.
- Valkey task가 바뀌면(Spot 회수, 장애) 데이터(인증번호, refresh token)가 사라진다.

### Secrets

| Secret | 내용 | 관리 |
|---|---|---|
| `deepple-dev/app` | `JWT_SECRET`, `MYSQL_PASSWORD` | Terraform이 랜덤 생성 (state에 남지 않음) |
| `deepple-dev/external` | App Store, Bizgo 키 | 직접 넣는다 |

`deepple-dev/external`은 키 이름이 `secrets.tf`의 `external_secret_keys`와 같은 JSON이다. 로컬 `.env`에서 만들어 넣을 수 있다.

```bash
set -a; source .env; set +a
jq -n 'env | {APP_STORE_KEY_ID, APP_STORE_PRIVATE_KEY_STRING, APP_STORE_ISSUER_ID, APP_STORE_APP_APPLE_ID,
  BIZGO_API_URL, BIZGO_API_KEY, BIZGO_CLIENT_ID, BIZGO_CLIENT_PASSWORD, BIZGO_FROM_PHONE_NUMBER}' \
  | aws secretsmanager put-secret-value --secret-id deepple-dev/external --secret-string file:///dev/stdin
```

### Google (Firebase, Google Play)

키 파일 없이 Workload Identity Federation을 쓴다. 앱 역할(`deepple-dev-app`)로 Google에 인증해 기존 서비스 계정을 impersonate한다.

1. 풀과 AWS 공급자를 만든다(프로젝트 `deepple-c5416`).
   ```bash
   gcloud services enable sts.googleapis.com iamcredentials.googleapis.com --project=deepple-c5416
   gcloud services enable iamcredentials.googleapis.com --project=atwoz-d63ef
   gcloud iam workload-identity-pools create aws --project=deepple-c5416 --location=global --display-name="AWS"
   gcloud iam workload-identity-pools providers create-aws deepple --project=deepple-c5416 --location=global \
     --workload-identity-pool=aws --account-id=339713092351 \
     --attribute-condition="attribute.aws_role == 'arn:aws:sts::339713092351:assumed-role/deepple-dev-app'"
   ```
2. 앱 역할에 두 서비스 계정의 impersonate 권한을 준다. `<번호>`는 `gcloud projects describe deepple-c5416 --format='value(projectNumber)'`로 확인한다.
   ```bash
   MEMBER="principalSet://iam.googleapis.com/projects/<번호>/locations/global/workloadIdentityPools/aws/attribute.aws_role/arn:aws:sts::339713092351:assumed-role/deepple-dev-app"
   gcloud iam service-accounts add-iam-policy-binding deepple-play-verifier@deepple-c5416.iam.gserviceaccount.com \
     --project=deepple-c5416 --role=roles/iam.workloadIdentityUser --member="$MEMBER"
   gcloud iam service-accounts add-iam-policy-binding firebase-adminsdk-ox847@atwoz-d63ef.iam.gserviceaccount.com \
     --project=atwoz-d63ef --role=roles/iam.workloadIdentityUser --member="$MEMBER"
   ```
3. `variables.tf`의 `gcp_workload_identity_provider`에 `projects/<번호>/locations/global/workloadIdentityPools/aws/providers/deepple`을 넣는다.

### DB 접속

RDS는 인터넷에 열려 있지 않다. 실행 중인 앱 태스크를 거쳐 로컬 포트로 연결한다([Session Manager 플러그인](https://docs.aws.amazon.com/systems-manager/latest/userguide/session-manager-working-with-install-plugin.html)이 필요하다).

```bash
CLUSTER=deepple-dev
TASK=$(aws ecs list-tasks --cluster $CLUSTER --service-name deepple-dev-api --query 'taskArns[0]' --output text | cut -d/ -f3)
RUNTIME=$(aws ecs describe-tasks --cluster $CLUSTER --tasks $TASK --query "tasks[0].containers[?name=='app'].runtimeId" --output text)
DB=$(terraform -chdir=infra/terraform/envs/dev output -raw db_address)
aws ssm start-session --target "ecs:${CLUSTER}_${TASK}_${RUNTIME}" \
  --document-name AWS-StartPortForwardingSessionToRemoteHost \
  --parameters "{\"host\":[\"$DB\"],\"portNumber\":[\"3306\"],\"localPortNumber\":[\"13306\"]}"
```

접속 주소는 `localhost:13306`, 계정은 `deepple`이다. 비밀번호는 `aws secretsmanager get-secret-value --secret-id deepple-dev/app`의 `MYSQL_PASSWORD` 값이다.

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
