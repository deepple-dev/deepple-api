output "ecr_repository_url" {
  description = "앱 이미지 저장소 주소"
  value       = aws_ecr_repository.this.repository_url
}

output "github_oidc_provider_arn" {
  description = "환경별 배포 역할의 신뢰 정책에 쓸 GitHub OIDC 공급자 ARN"
  value       = aws_iam_openid_connect_provider.github.arn
}

output "github_image_push_role_arn" {
  description = "GitHub Actions 이미지 push 역할 ARN"
  value       = aws_iam_role.github_image_push.arn
}

output "route53_zone_id" {
  description = "deepple.co.kr 호스팅 영역 ID"
  value       = aws_route53_zone.this.zone_id
}

output "alerts_topic_arn" {
  description = "알림 SNS 토픽 ARN. 환경별 알람의 대상으로 쓴다"
  value       = aws_sns_topic.alerts.arn
}

output "cloudtrail_bucket" {
  description = "CloudTrail 로그 버킷"
  value       = aws_s3_bucket.cloudtrail.bucket
}
