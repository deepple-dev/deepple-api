output "api_url" {
  description = "dev API 주소"
  value       = "https://${var.domain}"
}

output "ecs_cluster" {
  description = "ECS 클러스터 이름"
  value       = aws_ecs_cluster.this.name
}

output "ecs_service" {
  description = "ECS 서비스 이름"
  value       = aws_ecs_service.app.name
}

output "deploy_role_arn" {
  description = "GitHub Actions dev 배포 역할 ARN"
  value       = aws_iam_role.deploy.arn
}

output "alerts_topic_arn" {
  description = "알림 SNS 토픽 ARN. 배포 완료 알림을 발행한다"
  value       = module.alerts.topic_arn
}

output "db_address" {
  description = "RDS 주소. SSM 포트 포워딩의 대상 호스트"
  value       = aws_db_instance.this.address
}

output "app_role_arn" {
  description = "앱 역할 ARN. Google Workload Identity 공급자 조건에 쓴다"
  value       = aws_iam_role.app.arn
}

output "external_secret_name" {
  description = "외부 연동 키를 넣을 secret 이름"
  value       = aws_secretsmanager_secret.external.name
}

output "alerts_webhook_secret_name" {
  description = "#alerts-dev 웹훅 URL을 넣을 secret 이름"
  value       = module.alerts.webhook_secret_name
}
