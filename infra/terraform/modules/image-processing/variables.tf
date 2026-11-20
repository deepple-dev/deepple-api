variable "name" {
  description = "리소스 이름 접두어 (예: deepple-dev)"
  type        = string
}

variable "account_id" {
  description = "AWS 계정 ID"
  type        = string
}

variable "bucket_id" {
  description = "원본과 변환본을 두는 S3 버킷 이름"
  type        = string
}

variable "bucket_arn" {
  description = "원본과 변환본을 두는 S3 버킷 ARN"
  type        = string
}

variable "source_prefix" {
  description = "앱이 원본을 올리는 경로"
  type        = string
  default     = "tmp/"
}

variable "output_prefix" {
  description = "변환본을 저장하는 경로"
  type        = string
  default     = "images/"
}

variable "function_timeout_seconds" {
  description = "변환 Lambda 타임아웃"
  type        = number
  default     = 30
}

variable "function_memory_mb" {
  description = "변환 Lambda 메모리. CPU도 메모리에 비례해 늘어난다"
  type        = number
  default     = 1024
}

variable "maximum_concurrency" {
  description = "변환 큐를 동시에 처리하는 Lambda 최대 개수"
  type        = number
  default     = 5
}

variable "max_receive_count" {
  description = "DLQ로 옮기기 전까지 받는 최대 횟수"
  type        = number
  default     = 5
}

variable "log_retention_days" {
  description = "변환 Lambda 로그 보관 기간"
  type        = number
  default     = 30
}

variable "alarm_topic_arn" {
  description = "DLQ에 메시지가 쌓이면 알릴 SNS 토픽 ARN"
  type        = string
}
