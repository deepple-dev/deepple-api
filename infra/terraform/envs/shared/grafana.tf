resource "aws_secretsmanager_secret" "grafana_terraform" {
  name        = "deepple/grafana-terraform"
  description = "envs/grafana Terraform이 Grafana Cloud 스택에 접속할 때 쓰는 서비스 계정 토큰"
}
