ephemeral "tls_private_key" "jwt" {
  algorithm = "RSA"
  rsa_bits  = 2048
}

ephemeral "random_password" "db" {
  length           = 32
  override_special = "!#%^*-_=+"
}

resource "aws_secretsmanager_secret" "app" {
  name        = "${local.name}/app"
  description = "prod 앱 secret (DB 비밀번호)"
}

resource "aws_secretsmanager_secret_version" "app" {
  secret_id = aws_secretsmanager_secret.app.id
  secret_string_wo = jsonencode({
    MYSQL_PASSWORD = ephemeral.random_password.db.result
  })
  secret_string_wo_version = 1
}

resource "aws_secretsmanager_secret" "jwt" {
  name        = "${local.name}/jwt"
  description = "prod JWT 서명 키 (RSA 개인 키)"
}

resource "aws_secretsmanager_secret_version" "jwt" {
  secret_id = aws_secretsmanager_secret.jwt.id
  secret_string_wo = jsonencode({
    JWT_PRIVATE_KEY = ephemeral.tls_private_key.jwt.private_key_pem_pkcs8
  })
  secret_string_wo_version = 1
}

resource "aws_secretsmanager_secret" "external" {
  name        = "${local.name}/external"
  description = "prod 외부 연동 키 (App Store, Bizgo, Sentry)"
}

locals {
  external_secret_keys = [
    "APP_STORE_APP_APPLE_ID",
    "BIZGO_API_URL",
    "BIZGO_API_KEY",
    "BIZGO_FROM_PHONE_NUMBER",
    "SENTRY_DSN",
  ]
}
