module "alerts" {
  source = "../../modules/alerts"

  name                = "deepple-alerts"
  webhook_secret_name = "deepple/alerts/discord-webhook"
  mention_role_id     = var.discord_mention_role_id
  publisher_services  = ["budgets.amazonaws.com", "cloudwatch.amazonaws.com"]
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
    subscriber_sns_topic_arns = [module.alerts.topic_arn]
  }

  notification {
    comparison_operator       = "GREATER_THAN"
    threshold                 = 100
    threshold_type            = "PERCENTAGE"
    notification_type         = "ACTUAL"
    subscriber_sns_topic_arns = [module.alerts.topic_arn]
  }

  notification {
    comparison_operator       = "GREATER_THAN"
    threshold                 = 100
    threshold_type            = "PERCENTAGE"
    notification_type         = "FORECASTED"
    subscriber_sns_topic_arns = [module.alerts.topic_arn]
  }

  depends_on = [module.alerts]
}

moved {
  from = aws_sns_topic.alerts
  to   = module.alerts.aws_sns_topic.this
}

moved {
  from = aws_sns_topic_policy.alerts
  to   = module.alerts.aws_sns_topic_policy.this
}

moved {
  from = aws_secretsmanager_secret.discord_webhook
  to   = module.alerts.aws_secretsmanager_secret.webhook
}

moved {
  from = aws_cloudwatch_log_group.discord_notify
  to   = module.alerts.aws_cloudwatch_log_group.lambda
}

moved {
  from = aws_iam_role.discord_notify
  to   = module.alerts.aws_iam_role.lambda
}

moved {
  from = aws_iam_role_policy.discord_notify
  to   = module.alerts.aws_iam_role_policy.lambda
}

moved {
  from = aws_lambda_function.discord_notify
  to   = module.alerts.aws_lambda_function.this
}

moved {
  from = aws_lambda_permission.discord_notify_sns
  to   = module.alerts.aws_lambda_permission.sns
}

moved {
  from = aws_sns_topic_subscription.discord_notify
  to   = module.alerts.aws_sns_topic_subscription.lambda
}
