# AWS Budgets가 암호화된 토픽에는 발행하지 못해 암호화하지 않는다
resource "aws_sns_topic" "alerts" {
  name = "deepple-alerts"
}

data "aws_iam_policy_document" "alerts_topic" {
  statement {
    sid       = "AllowAccountOwner"
    actions   = ["SNS:Publish", "SNS:Subscribe", "SNS:GetTopicAttributes", "SNS:ListSubscriptionsByTopic"]
    resources = [aws_sns_topic.alerts.arn]

    principals {
      type        = "AWS"
      identifiers = ["arn:aws:iam::${var.account_id}:root"]
    }
  }

  statement {
    sid       = "AllowServicePublish"
    actions   = ["SNS:Publish"]
    resources = [aws_sns_topic.alerts.arn]

    principals {
      type        = "Service"
      identifiers = ["budgets.amazonaws.com", "cloudwatch.amazonaws.com"]
    }

    condition {
      test     = "StringEquals"
      variable = "aws:SourceAccount"
      values   = [var.account_id]
    }
  }
}

resource "aws_sns_topic_policy" "alerts" {
  arn    = aws_sns_topic.alerts.arn
  policy = data.aws_iam_policy_document.alerts_topic.json
}

# 값(웹훅 URL)은 state에 남지 않도록 Terraform 밖에서 콘솔로 넣는다
resource "aws_secretsmanager_secret" "discord_webhook" {
  name        = "deepple/alerts/discord-webhook"
  description = "알림을 보낼 Discord 웹훅 URL"
}

data "archive_file" "discord_notify" {
  type        = "zip"
  source_dir  = "${path.module}/lambda/discord_notify"
  output_path = "${path.module}/build/discord_notify.zip"
}

resource "aws_cloudwatch_log_group" "discord_notify" {
  name              = "/aws/lambda/deepple-alerts-discord"
  retention_in_days = 30
}

data "aws_iam_policy_document" "discord_notify_trust" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "discord_notify" {
  name               = "deepple-alerts-discord"
  description        = "Forward deepple-alerts messages to Discord"
  assume_role_policy = data.aws_iam_policy_document.discord_notify_trust.json
}

data "aws_iam_policy_document" "discord_notify" {
  statement {
    sid       = "WriteLogs"
    actions   = ["logs:CreateLogStream", "logs:PutLogEvents"]
    resources = ["${aws_cloudwatch_log_group.discord_notify.arn}:log-stream:*"]
  }

  statement {
    sid       = "ReadWebhook"
    actions   = ["secretsmanager:GetSecretValue"]
    resources = [aws_secretsmanager_secret.discord_webhook.arn]
  }
}

resource "aws_iam_role_policy" "discord_notify" {
  name   = "discord-notify"
  role   = aws_iam_role.discord_notify.id
  policy = data.aws_iam_policy_document.discord_notify.json
}

resource "aws_lambda_function" "discord_notify" {
  function_name    = "deepple-alerts-discord"
  role             = aws_iam_role.discord_notify.arn
  runtime          = "python3.13"
  architectures    = ["arm64"]
  handler          = "index.handler"
  filename         = data.archive_file.discord_notify.output_path
  source_code_hash = data.archive_file.discord_notify.output_base64sha256
  timeout          = 15

  environment {
    variables = {
      WEBHOOK_SECRET_ARN = aws_secretsmanager_secret.discord_webhook.arn
      MENTION_ROLE_ID    = var.discord_mention_role_id
    }
  }

  logging_config {
    log_format = "Text"
    log_group  = aws_cloudwatch_log_group.discord_notify.name
  }

  depends_on = [aws_iam_role_policy.discord_notify]
}

resource "aws_lambda_permission" "discord_notify_sns" {
  statement_id  = "AllowAlertsTopic"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.discord_notify.function_name
  principal     = "sns.amazonaws.com"
  source_arn    = aws_sns_topic.alerts.arn
}

resource "aws_sns_topic_subscription" "discord_notify" {
  topic_arn = aws_sns_topic.alerts.arn
  protocol  = "lambda"
  endpoint  = aws_lambda_function.discord_notify.arn

  depends_on = [aws_lambda_permission.discord_notify_sns]
}

resource "aws_budgets_budget" "monthly" {
  name         = "deepple-monthly"
  budget_type  = "COST"
  limit_amount = tostring(var.monthly_budget_usd)
  limit_unit   = "USD"
  time_unit    = "MONTHLY"

  notification {
    comparison_operator       = "GREATER_THAN"
    threshold                 = 80
    threshold_type            = "PERCENTAGE"
    notification_type         = "ACTUAL"
    subscriber_sns_topic_arns = [aws_sns_topic.alerts.arn]
  }

  notification {
    comparison_operator       = "GREATER_THAN"
    threshold                 = 100
    threshold_type            = "PERCENTAGE"
    notification_type         = "ACTUAL"
    subscriber_sns_topic_arns = [aws_sns_topic.alerts.arn]
  }

  notification {
    comparison_operator       = "GREATER_THAN"
    threshold                 = 100
    threshold_type            = "PERCENTAGE"
    notification_type         = "FORECASTED"
    subscriber_sns_topic_arns = [aws_sns_topic.alerts.arn]
  }

  depends_on = [aws_sns_topic_policy.alerts]
}
