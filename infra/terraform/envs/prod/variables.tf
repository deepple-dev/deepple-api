variable "region" {
  description = "AWS 리전"
  type        = string
  default     = "ap-northeast-2"
}

variable "account_id" {
  description = "AWS 계정 ID"
  type        = string
  default     = "339713092351"
}

variable "images_domain" {
  description = "이미지 CloudFront 도메인"
  type        = string
  default     = "images.deepple.co.kr"
}

variable "nat_gateway_per_az" {
  description = "AZ마다 NAT 게이트웨이를 둘지 여부. false면 NAT 1개를 모든 AZ가 같이 쓴다"
  type        = bool
  default     = false
}

variable "db" {
  description = "RDS 설정"
  type = object({
    instance_class          = string
    multi_az                = bool
    allocated_storage       = number
    max_allocated_storage   = number
    backup_retention_period = number
  })
  default = {
    instance_class          = "db.t4g.small"
    multi_az                = false
    allocated_storage       = 20
    max_allocated_storage   = 100
    backup_retention_period = 14
  }
}

variable "domain" {
  description = "API 도메인"
  type        = string
  default     = "api.deepple.co.kr"
}

variable "image_tag" {
  description = "처음 만들 때 쓸 앱 이미지 태그(커밋 SHA). 이후 배포는 CD가 task definition을 갱신한다"
  type        = string
  default     = "220048dd051d61e73d805481a63b4396b424d9ec"
}

variable "app" {
  description = "앱 task 크기와 개수. on_demand_base만큼은 일반 Fargate, 나머지는 Spot으로 띄운다"
  type = object({
    cpu            = number
    memory         = number
    min_count      = number
    max_count      = number
    on_demand_base = number
    cpu_target     = number
  })
  default = {
    cpu            = 1024
    memory         = 2048
    min_count      = 2
    max_count      = 4
    on_demand_base = 1
    cpu_target     = 60
  }
}

variable "discord_mention_role_id" {
  description = "알림 메시지에서 멘션할 Discord 역할 ID"
  type        = string
  default     = "1394957566953652256"
}

variable "gcp_workload_identity_provider" {
  description = "Google Cloud Workload Identity 공급자 전체 이름 (projects/<번호>/locations/global/workloadIdentityPools/<풀>/providers/<공급자>)"
  type        = string
}

variable "firebase_project_id" {
  description = "Firebase 프로젝트 ID"
  type        = string
  default     = "deepple-c5416"
}

variable "firebase_service_account" {
  description = "앱이 impersonate할 Firebase 서비스 계정"
  type        = string
  default     = "firebase-adminsdk-fbsvc@deepple-c5416.iam.gserviceaccount.com"
}

variable "google_play_service_account" {
  description = "앱이 impersonate할 Google Play 서비스 계정"
  type        = string
  default     = "deepple-play-verifier@deepple-c5416.iam.gserviceaccount.com"
}

variable "alloy_version" {
  description = "Grafana Alloy 이미지 버전"
  type        = string
  default     = "v1.21.3"
}

variable "grafana_cloud_otlp_endpoint" {
  description = "Grafana Cloud OTLP 엔드포인트"
  type        = string
  default     = "https://otlp-gateway-prod-ap-northeast-0.grafana.net/otlp"
}

variable "grafana_cloud_instance_id" {
  description = "Grafana Cloud 스택 인스턴스 ID"
  type        = string
  default     = "1902666"
}
