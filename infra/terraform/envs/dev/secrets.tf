# secret 값은 ephemeral로 만들어 state에 남기지 않는다
ephemeral "tls_private_key" "jwt" {
  algorithm = "RSA"
  rsa_bits  = 2048
}

# RDS가 허용하지 않는 문자(/ @ " 공백)는 뺀다
ephemeral "random_password" "db" {
  length           = 32
  override_special = "!#%^*-_=+"
}

resource "aws_secretsmanager_secret" "app" {
  name        = "${local.name}/app"
  description = "dev 앱 secret (DB 비밀번호)"
}

resource "aws_secretsmanager_secret_version" "app" {
  secret_id = aws_secretsmanager_secret.app.id
  secret_string_wo = jsonencode({
    MYSQL_PASSWORD = ephemeral.random_password.db.result
  })
  # 값을 다시 만들려면 버전을 올린다. RDS의 password_wo_version도 함께 올려야 한다
  secret_string_wo_version = 1
}

resource "aws_secretsmanager_secret" "jwt" {
  name        = "${local.name}/jwt"
  description = "dev JWT 서명 키 (RSA 개인 키)"
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
  description = "dev 외부 연동 키 (App Store, Bizgo, Sentry)"
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
