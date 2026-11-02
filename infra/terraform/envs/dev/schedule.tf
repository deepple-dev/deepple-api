locals {
  schedule_timezone = "Asia/Seoul"
  schedule_state    = var.nightly_shutdown.enabled ? "ENABLED" : "DISABLED"
  stop_hour         = var.nightly_shutdown.stop_hour
  start_hour        = var.nightly_shutdown.start_hour
}

data "aws_iam_policy_document" "scheduler_trust" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["scheduler.amazonaws.com"]
    }

    condition {
      test     = "StringEquals"
      variable = "aws:SourceAccount"
      values   = [var.account_id]
    }
  }
}

resource "aws_iam_role" "scheduler" {
  name               = "${local.name}-scheduler"
  description        = "Stop and start deepple-dev ECS and RDS on schedule"
  assume_role_policy = data.aws_iam_policy_document.scheduler_trust.json
}

data "aws_iam_policy_document" "scheduler" {
  statement {
    actions   = ["ecs:UpdateService"]
    resources = [aws_ecs_service.app.id]
  }

  statement {
    actions   = ["rds:StopDBInstance", "rds:StartDBInstance"]
    resources = [aws_db_instance.this.arn]
  }
}

resource "aws_iam_role_policy" "scheduler" {
  name   = "stop-start"
  role   = aws_iam_role.scheduler.id
  policy = data.aws_iam_policy_document.scheduler.json
}

resource "aws_scheduler_schedule" "ecs_stop" {
  name                         = "${local.name}-ecs-stop"
  schedule_expression          = "cron(0 ${local.stop_hour} * * ? *)"
  schedule_expression_timezone = local.schedule_timezone
  state                        = local.schedule_state

  flexible_time_window {
    mode = "OFF"
  }

  target {
    arn      = "arn:aws:scheduler:::aws-sdk:ecs:updateService"
    role_arn = aws_iam_role.scheduler.arn
    input = jsonencode({
      Cluster      = aws_ecs_cluster.this.name
      Service      = aws_ecs_service.app.name
      DesiredCount = 0
    })
  }
}

resource "aws_scheduler_schedule" "rds_stop" {
  name                         = "${local.name}-rds-stop"
  schedule_expression          = "cron(5 ${local.stop_hour} * * ? *)"
  schedule_expression_timezone = local.schedule_timezone
  state                        = local.schedule_state

  flexible_time_window {
    mode = "OFF"
  }

  target {
    arn      = "arn:aws:scheduler:::aws-sdk:rds:stopDBInstance"
    role_arn = aws_iam_role.scheduler.arn
    input    = jsonencode({ DbInstanceIdentifier = aws_db_instance.this.identifier })
  }
}

# RDS가 켜지는 데 몇 분 걸려서 앱은 15분 뒤에 켠다
resource "aws_scheduler_schedule" "rds_start" {
  name                         = "${local.name}-rds-start"
  schedule_expression          = "cron(0 ${local.start_hour} * * ? *)"
  schedule_expression_timezone = local.schedule_timezone
  state                        = local.schedule_state

  flexible_time_window {
    mode = "OFF"
  }

  target {
    arn      = "arn:aws:scheduler:::aws-sdk:rds:startDBInstance"
    role_arn = aws_iam_role.scheduler.arn
    input    = jsonencode({ DbInstanceIdentifier = aws_db_instance.this.identifier })
  }
}

resource "aws_scheduler_schedule" "ecs_start" {
  name                         = "${local.name}-ecs-start"
  schedule_expression          = "cron(15 ${local.start_hour} * * ? *)"
  schedule_expression_timezone = local.schedule_timezone
  state                        = local.schedule_state

  flexible_time_window {
    mode = "OFF"
  }

  target {
    arn      = "arn:aws:scheduler:::aws-sdk:ecs:updateService"
    role_arn = aws_iam_role.scheduler.arn
    input = jsonencode({
      Cluster      = aws_ecs_cluster.this.name
      Service      = aws_ecs_service.app.name
      DesiredCount = 1
    })
  }
}
