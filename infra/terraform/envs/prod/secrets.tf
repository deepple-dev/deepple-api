ephemeral "random_password" "jwt" {
  length  = 64
  special = false
}

ephemeral "random_password" "db" {
  length           = 32
  override_special = "!#%^*-_=+"
}

resource "aws_secretsmanager_secret" "app" {
  name        = "${local.name}/app"
  description = "prod 앱 secret (JWT 키, DB 비밀번호)"
}

resource "aws_secretsmanager_secret_version" "app" {
  secret_id = aws_secretsmanager_secret.app.id
  secret_string_wo = jsonencode({
    JWT_SECRET     = ephemeral.random_password.jwt.result
    MYSQL_PASSWORD = ephemeral.random_password.db.result
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
