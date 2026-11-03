variable "name" {
  description = "SNS 토픽 이름. Lambda·역할·로그 그룹 이름의 접두어로도 쓴다"
  type        = string
}

variable "webhook_secret_name" {
  description = "Discord 웹훅 URL을 넣을 Secrets Manager secret 이름"
  type        = string
}

variable "mention_role_id" {
  description = "알림 메시지에서 멘션할 Discord 역할 ID"
  type        = string
}

variable "publisher_services" {
  description = "토픽에 발행할 수 있는 AWS 서비스 (예: cloudwatch.amazonaws.com)"
  type        = list(string)
  default     = ["cloudwatch.amazonaws.com"]
}
