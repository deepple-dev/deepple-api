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
  description = "ECS task 시작 실패, 배포 실패"
  event_pattern = jsonencode({
    source      = ["aws.ecs"]
    detail-type = ["ECS Service Action", "ECS Deployment State Change"]
    resources   = [aws_ecs_service.app.id, aws_ecs_service.valkey.id]
    detail = {
      eventName = ["SERVICE_TASK_START_IMPAIRED", "SERVICE_DEPLOYMENT_FAILED"]
    }
  })
}

resource "aws_cloudwatch_event_target" "ecs_failure" {
  rule = aws_cloudwatch_event_rule.ecs_failure.name
  arn  = module.alerts.topic_arn
}

# 야간 중지 시간에는 지표가 없으므로 데이터가 없으면 정상으로 본다
resource "aws_cloudwatch_metric_alarm" "alb_5xx" {
  alarm_name          = "${local.name}-alb-5xx"
  alarm_description   = "dev 서버 오류\n서버 오류(5xx) 응답이 발생했습니다."
  namespace           = "AWS/ApplicationELB"
  metric_name         = "HTTPCode_Target_5XX_Count"
  dimensions          = { LoadBalancer = aws_lb.this.arn_suffix }
  statistic           = "Sum"
  period              = 60
  evaluation_periods  = 1
  threshold           = 1
  comparison_operator = "GreaterThanOrEqualToThreshold"
  treat_missing_data  = "notBreaching"
  alarm_actions       = [module.alerts.topic_arn]
  ok_actions          = [module.alerts.topic_arn]
}

resource "aws_cloudwatch_metric_alarm" "app_memory" {
  alarm_name          = "${local.name}-app-memory"
  alarm_description   = "dev 서버 메모리 부족\n앱 메모리 사용률이 10분 동안 90%를 넘었습니다."
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
  alarm_name          = "${local.name}-db-storage"
  alarm_description   = "dev DB 저장 공간 부족\nDB 남은 저장 공간이 2GB 아래로 떨어졌습니다."
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
