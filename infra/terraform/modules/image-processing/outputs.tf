output "function_arn" {
  description = "변환 Lambda ARN"
  value       = aws_lambda_function.this.arn
}

output "function_name" {
  description = "변환 Lambda 이름"
  value       = aws_lambda_function.this.function_name
}

output "results_queue_name" {
  description = "변환 결과 큐 이름. 백엔드가 소비한다"
  value       = aws_sqs_queue.results.name
}

output "results_queue_arn" {
  description = "변환 결과 큐 ARN"
  value       = aws_sqs_queue.results.arn
}

output "uploads_dlq_name" {
  description = "변환 실패 메시지가 쌓이는 DLQ 이름"
  value       = aws_sqs_queue.uploads_dlq.name
}

output "results_dlq_name" {
  description = "결과 처리 실패 메시지가 쌓이는 DLQ 이름"
  value       = aws_sqs_queue.results_dlq.name
}
