output "state_bucket" {
  description = "각 환경의 backend 설정에 넣을 state 버킷 이름"
  value       = aws_s3_bucket.state.bucket
}
