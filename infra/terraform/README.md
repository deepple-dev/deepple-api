# Terraform

딥플 AWS 인프라 코드. 계정 하나(`339713092351`), 리전 `ap-northeast-2`에서 환경(dev, prod)을 나눠 관리한다.

## 구조

```
infra/terraform/
├── bootstrap/   # state 저장용 S3 버킷. 계정당 1회, 완료됨
├── modules/     # 직접 작성한 재사용 모듈 (ECS, RDS 등)
└── envs/
    ├── shared/  # 계정 공통 (ECR, GitHub OIDC, Route 53, 알림, CloudTrail)
    ├── dev/
    └── prod/
```

`envs/`, `modules/`는 작성 중이다.

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

`bootstrap/`은 실행하지 않는다. (아래 [bootstrap](#bootstrap) 참고)

## 알림

계정 알림(예산 초과, 루트 계정 사용)은 SNS 토픽 `deepple-alerts` → Lambda → Discord `#alerts-account`로 온다.

- 웹훅 URL은 Secrets Manager `deepple/alerts/discord-webhook`에 있다. Terraform은 빈 비밀만 만들고 값은 직접 넣는다. 웹훅을 새로 만들면 Discord에서 URL을 복사한 뒤 실행한다.
  ```bash
  aws secretsmanager put-secret-value --secret-id deepple/alerts/discord-webhook --secret-string "$(pbpaste)"
  ```
- 멘션 역할은 `envs/shared/variables.tf`의 `discord_mention_role_id`.
- 경보(CloudWatch Alarm)의 `alarm_description`은 첫 줄이 Discord 메시지 제목, 나머지가 본문이 된다.

## 작업 흐름

```bash
terraform fmt -recursive   # 포맷
terraform validate         # 문법 검사
terraform plan             # 변경 미리보기 (AWS는 바뀌지 않음)
terraform apply            # 실제 적용
```

- 코드 변경은 다른 코드와 같이 이슈 → 브랜치 → PR로 올린다.
- `apply` 전에 plan 결과의 생성·변경·삭제 수를 반드시 확인한다. 특히 삭제(`destroy`)와 교체(`replace`)가 있으면 이유를 확인한다.
- 환경 순서는 `shared` → `dev` → `prod`. dev에서 먼저 확인하고 prod에 적용한다.

## state

- 저장 위치: S3 버킷 `deepple-terraform-state-339713092351`
- 환경마다 key를 나눈다. 예: `envs/dev/terraform.tfstate`
- 잠금: S3 자체 잠금(`use_lockfile = true`). 같은 환경에 동시에 apply할 수 없다.
- 버전 관리가 켜져 있어 state가 깨지면 S3 콘솔에서 이전 버전으로 되돌릴 수 있다. 이전 버전은 90일 뒤 삭제되고, 최근 10개는 기간과 관계없이 남는다.

## bootstrap

state 버킷을 만드는 설정이다. **2026-11-01에 적용 완료됐고, 다시 실행하지 않는다.**

- state를 저장할 버킷을 Terraform으로 만들려면 그 버킷의 state를 둘 곳이 따로 필요하다. 그래서 bootstrap만 로컬 state로 버킷을 만든다.
- bootstrap의 state(`bootstrap/terraform.tfstate`)는 처음 적용한 사람의 컴퓨터에만 있고 커밋하지 않는다. 다른 컴퓨터에서 실행하면 버킷을 새로 만들려다 "이미 있음" 오류가 난다.
- 버킷 설정을 바꿔야 하면, 로컬 state가 없는 컴퓨터에서는 먼저 기존 버킷을 가져온다(`terraform import`).
- 새 AWS 계정에 처음 구축할 때만 아래를 실행한다.
  ```bash
  cd infra/terraform/bootstrap
  terraform init
  terraform apply
  ```

버킷 설정: 버전 관리, SSE-S3 암호화, 퍼블릭 접근 차단, ACL 비활성화, HTTPS 외 요청 거부, `prevent_destroy`(Terraform으로 삭제 불가)

## 규칙

- `.terraform.lock.hcl`은 커밋한다. provider를 추가·업그레이드하면 CI(Linux)용 해시도 함께 기록한다.
  ```bash
  terraform providers lock -platform=darwin_arm64 -platform=linux_amd64
  ```
- 비밀 값은 코드나 tfvars에 넣지 않는다. Secrets Manager를 쓴다.
- IAM 역할·정책의 `description`은 영문으로 쓴다. 한글은 apply 때 AWS가 거부한다.
- 커뮤니티 모듈은 버전을 고정한다. (VPC는 `terraform-aws-modules/vpc/aws`)
- AWS 콘솔에서 Terraform이 관리하는 리소스를 직접 수정하지 않는다. 다음 apply 때 코드 상태로 되돌아간다.
- 모든 리소스에는 provider의 `default_tags`로 `Project`, `Stack`, `ManagedBy` 태그가 붙는다.
