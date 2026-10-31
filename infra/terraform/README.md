# Terraform

AWS 인프라 코드. 리전 `ap-northeast-2`, 계정 하나에 환경(dev, prod)을 나눠 관리한다.

## 구조

```
infra/terraform/
├── bootstrap/   # state 저장용 S3 버킷 (최초 1회)
├── modules/     # 재사용 모듈
└── envs/
    ├── shared/  # 환경 공통 (ECR, GitHub OIDC 등)
    ├── dev/
    └── prod/
```

## 요구 사항

- Terraform 1.11 이상
- AWS 자격 증명 (`aws configure` 또는 SSO)

## 최초 설정: bootstrap

state 버킷을 만든다. bootstrap 자체의 state는 로컬(`bootstrap/terraform.tfstate`)에 남고 커밋하지 않는다.

```bash
cd infra/terraform/bootstrap
terraform init
terraform apply
```

출력된 `state_bucket` 이름을 각 환경의 backend 설정에 사용한다.

## 규칙

- state는 환경별로 분리된 key에 저장하고, S3 자체 잠금(`use_lockfile = true`)을 쓴다.
- `.terraform.lock.hcl`은 커밋한다. (provider 버전 고정)
- 비밀 값은 코드나 tfvars에 넣지 않는다. Secrets Manager를 쓴다.
- 커뮤니티 모듈은 버전을 고정한다.
