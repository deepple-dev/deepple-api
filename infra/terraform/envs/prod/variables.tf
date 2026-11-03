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
