output "topic_arn" {
  description = "알림 SNS 토픽 ARN. 경보의 alarm_actions에 쓴다"
  value       = aws_sns_topic.this.arn
}

output "webhook_secret_name" {
  description = "웹훅 URL을 넣을 secret 이름"
  value       = aws_secretsmanager_secret.webhook.name
}

output "function_name" {
  description = "Discord 전송 Lambda 이름. 오류 알람에 쓴다"
  value       = aws_lambda_function.this.function_name
}
