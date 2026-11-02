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

variable "domain" {
  description = "API 도메인"
  type        = string
  default     = "dev-api.deepple.co.kr"
}

variable "github_repository" {
  description = "OIDC로 배포할 GitHub 저장소 (owner/name)"
  type        = string
  default     = "deepple-dev/deepple-api"
}

variable "image_tag" {
  description = "처음 만들 때 쓸 앱 이미지 태그(커밋 SHA). 이후 배포는 CD가 task definition을 갱신한다"
  type        = string
  default     = "84d340e00ebc77054b39ace23090fd606774983a"
}

variable "nightly_shutdown" {
  description = "야간 중지 설정. 시각은 KST 정시이고, 앱은 DB보다 5분 먼저 끄고 DB가 켜진 15분 뒤에 켠다"
  type = object({
    enabled    = bool
    stop_hour  = number
    start_hour = number
  })
  default = {
    enabled    = true
    stop_hour  = 2
    start_hour = 8
  }

  validation {
    condition = alltrue([
      for hour in [var.nightly_shutdown.stop_hour, var.nightly_shutdown.start_hour] : hour >= 0 && hour <= 23 && floor(hour) == hour
    ]) && var.nightly_shutdown.stop_hour != var.nightly_shutdown.start_hour
    error_message = "stop_hour와 start_hour는 0~23의 서로 다른 정수여야 한다."
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
  default     = "atwoz-d63ef"
}

variable "firebase_service_account" {
  description = "앱이 impersonate할 Firebase 서비스 계정"
  type        = string
  default     = "firebase-adminsdk-ox847@atwoz-d63ef.iam.gserviceaccount.com"
}

variable "google_play_service_account" {
  description = "앱이 impersonate할 Google Play 서비스 계정"
  type        = string
  default     = "deepple-play-verifier@deepple-c5416.iam.gserviceaccount.com"
}
