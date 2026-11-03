module "alerts" {
  source = "../../modules/alerts"

  name                = "${local.name}-alerts"
  webhook_secret_name = "${local.name}/alerts/discord-webhook"
  mention_role_id     = var.discord_mention_role_id
  publisher_services  = ["cloudwatch.amazonaws.com", "events.amazonaws.com"]
}

# 컨테이너가 시작하자마자 죽으면 ALB에 등록되지 않아 alarm으로는 잡지 못한다
resource "aws_cloudwatch_event_rule" "ecs_failure" {
  name        = "${local.name}-ecs-failure"
  description = "ECS task 시작·배치 실패, 배포 실패"
  event_pattern = jsonencode({
    source      = ["aws.ecs"]
    detail-type = ["ECS Service Action", "ECS Deployment State Change"]
    resources   = [aws_ecs_service.app.id, aws_ecs_service.valkey.id, aws_ecs_service.alloy.id]
    detail = {
      eventName = ["SERVICE_TASK_START_IMPAIRED", "SERVICE_TASK_PLACEMENT_FAILURE", "SERVICE_DEPLOYMENT_FAILED"]
    }
  })
}

resource "aws_cloudwatch_event_target" "ecs_failure" {
  rule = aws_cloudwatch_event_rule.ecs_failure.name
  arn  = module.alerts.topic_arn
}

# 야간 중지 시간에는 지표가 없으므로 데이터가 없으면 정상으로 본다
resource "aws_cloudwatch_metric_alarm" "app_memory" {
  alarm_name = "${local.name}-app-memory"
  alarm_description = jsonencode({
    title = "High memory usage (${aws_ecs_service.app.name})"
    what  = "앱 컨테이너 메모리 사용률 {value} ({condition}, 10분 평균)"
    why   = ["메모리 누수", "트래픽 증가", "ECS task 메모리(${aws_ecs_task_definition.app.memory / 1024}GB) 부족"]
    how   = ["Grafana deepple-api 대시보드에서 JVM heap·GC 확인", "최근 배포 확인"]
    unit  = "percent"
  })
  namespace           = "AWS/ECS"
  metric_name         = "MemoryUtilization"
  dimensions          = { ClusterName = aws_ecs_cluster.this.name, ServiceName = aws_ecs_service.app.name }
  statistic           = "Average"
  period              = 300
  evaluation_periods  = 2
  threshold           = 90
  comparison_operator = "GreaterThanThreshold"
  treat_missing_data  = "notBreaching"
  alarm_actions       = [module.alerts.topic_arn]
  ok_actions          = [module.alerts.topic_arn]
}

resource "aws_cloudwatch_metric_alarm" "db_storage" {
  alarm_name = "${local.name}-db-storage"
  alarm_description = jsonencode({
    title = "Low DB storage (${aws_db_instance.this.identifier})"
    what  = "RDS 남은 저장 공간 {value} ({condition}, 전체 ${aws_db_instance.this.allocated_storage}GB)"
    why   = ["데이터 증가", "바이너리 로그 누적"]
    how   = ["`/dev-connect`로 접속해 큰 테이블 확인", "Terraform `allocated_storage`로 저장 공간 확장"]
    unit  = "bytes"
  })
  namespace           = "AWS/RDS"
  metric_name         = "FreeStorageSpace"
  dimensions          = { DBInstanceIdentifier = aws_db_instance.this.identifier }
  statistic           = "Minimum"
  period              = 300
  evaluation_periods  = 1
  threshold           = 2 * 1024 * 1024 * 1024
  comparison_operator = "LessThanThreshold"
  treat_missing_data  = "notBreaching"
  alarm_actions       = [module.alerts.topic_arn]
  ok_actions          = [module.alerts.topic_arn]
}

# 알림 Lambda가 고장 나면 자기 고장을 알릴 수 없으므로 계정 공통 채널(#alerts-account)로 보낸다
data "aws_sns_topic" "account_alerts" {
  name = "deepple-alerts"
}

resource "aws_cloudwatch_metric_alarm" "alerts_lambda_errors" {
  alarm_name = "${local.name}-alerts-lambda-errors"
  alarm_description = jsonencode({
    title = "Alert delivery failing (${module.alerts.function_name})"
    what  = "#alerts-dev 알림 Lambda 오류 {value} (5분간), 그동안의 dev 알림 유실 가능"
    why   = ["Discord 웹훅 변경·삭제", "Lambda 코드 오류"]
    how   = ["CloudWatch `/aws/lambda/${module.alerts.function_name}`에서 오류 로그 확인", "웹훅 secret `${local.name}/alerts/discord-webhook` 값 확인"]
    unit  = "count"
  })
  namespace           = "AWS/Lambda"
  metric_name         = "Errors"
  dimensions          = { FunctionName = module.alerts.function_name }
  statistic           = "Sum"
  period              = 300
  evaluation_periods  = 1
  threshold           = 1
  comparison_operator = "GreaterThanOrEqualToThreshold"
  treat_missing_data  = "notBreaching"
  alarm_actions       = [data.aws_sns_topic.account_alerts.arn]
  ok_actions          = [data.aws_sns_topic.account_alerts.arn]
}
