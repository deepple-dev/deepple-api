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

variable "github_repository" {
  description = "OIDC로 AWS에 접근할 GitHub 저장소 (owner/name)"
  type        = string
  default     = "deepple-dev/deepple-api"
}

variable "discord_mention_role_id" {
  description = "알림 메시지에서 멘션할 Discord 역할 ID"
  type        = string
  default     = "1394957566953652256"
}

variable "monthly_budget_usd" {
  description = "월 예산(USD). 80%·100% 도달과 100% 초과 예측 시 알림"
  type        = number
  default     = 200
}
