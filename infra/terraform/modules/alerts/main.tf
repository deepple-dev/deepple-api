data "aws_caller_identity" "current" {}

locals {
  account_id    = data.aws_caller_identity.current.account_id
  function_name = "${var.name}-discord"
}

# AWS Budgets가 암호화된 토픽에는 발행하지 못해 암호화하지 않는다
resource "aws_sns_topic" "this" {
  name = var.name
}

data "aws_iam_policy_document" "topic" {
  statement {
    sid       = "AllowAccountOwner"
    actions   = ["SNS:Publish", "SNS:Subscribe", "SNS:GetTopicAttributes", "SNS:ListSubscriptionsByTopic"]
    resources = [aws_sns_topic.this.arn]

    principals {
      type        = "AWS"
      identifiers = ["arn:aws:iam::${local.account_id}:root"]
    }
  }

  statement {
    sid       = "AllowServicePublish"
    actions   = ["SNS:Publish"]
    resources = [aws_sns_topic.this.arn]

    principals {
      type        = "Service"
      identifiers = var.publisher_services
    }

    condition {
      test     = "StringEquals"
      variable = "aws:SourceAccount"
      values   = [local.account_id]
    }
  }
}

resource "aws_sns_topic_policy" "this" {
  arn    = aws_sns_topic.this.arn
  policy = data.aws_iam_policy_document.topic.json
}

# 값(웹훅 URL)은 state에 남지 않도록 Terraform 밖에서 넣는다
resource "aws_secretsmanager_secret" "webhook" {
  name        = var.webhook_secret_name
  description = "알림을 보낼 Discord 웹훅 URL"
}

data "archive_file" "lambda" {
  type        = "zip"
  source_dir  = "${path.module}/lambda"
  output_path = "${path.root}/build/discord_notify.zip"
}

resource "aws_cloudwatch_log_group" "lambda" {
  name              = "/aws/lambda/${local.function_name}"
  retention_in_days = 30
}

data "aws_iam_policy_document" "lambda_trust" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "lambda" {
  name               = local.function_name
  description        = "Forward ${var.name} messages to Discord"
  assume_role_policy = data.aws_iam_policy_document.lambda_trust.json
}

data "aws_iam_policy_document" "lambda" {
  statement {
    sid       = "WriteLogs"
    actions   = ["logs:CreateLogStream", "logs:PutLogEvents"]
    resources = ["${aws_cloudwatch_log_group.lambda.arn}:log-stream:*"]
  }

  statement {
    sid       = "ReadAlarmHistory"
    actions   = ["cloudwatch:DescribeAlarmHistory"]
    resources = ["arn:aws:cloudwatch:*:${local.account_id}:alarm:*"]
  }

  statement {
    sid       = "ReadWebhook"
    actions   = ["secretsmanager:GetSecretValue"]
    resources = [aws_secretsmanager_secret.webhook.arn]
  }
}

resource "aws_iam_role_policy" "lambda" {
  name   = "discord-notify"
  role   = aws_iam_role.lambda.id
  policy = data.aws_iam_policy_document.lambda.json
}

resource "aws_lambda_function" "this" {
  function_name    = local.function_name
  role             = aws_iam_role.lambda.arn
  runtime          = "python3.13"
  architectures    = ["arm64"]
  handler          = "index.handler"
  filename         = data.archive_file.lambda.output_path
  source_code_hash = data.archive_file.lambda.output_base64sha256
  timeout          = 15

  environment {
    variables = {
      WEBHOOK_SECRET_ARN = aws_secretsmanager_secret.webhook.arn
      MENTION_ROLE_ID    = var.mention_role_id
    }
  }

  logging_config {
    log_format = "Text"
    log_group  = aws_cloudwatch_log_group.lambda.name
  }

  depends_on = [aws_iam_role_policy.lambda]
}

resource "aws_lambda_permission" "sns" {
  statement_id  = "AllowAlertsTopic"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.this.function_name
  principal     = "sns.amazonaws.com"
  source_arn    = aws_sns_topic.this.arn
}

resource "aws_sns_topic_subscription" "lambda" {
  topic_arn = aws_sns_topic.this.arn
  protocol  = "lambda"
  endpoint  = aws_lambda_function.this.arn

  depends_on = [aws_lambda_permission.sns]
}
