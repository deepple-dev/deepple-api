module "alerts" {
  source = "../../modules/alerts"

  name                = "${local.name}-alerts"
  webhook_secret_name = "${local.name}/alerts/discord-webhook"
  mention_role_id     = var.discord_mention_role_id
  publisher_services  = ["cloudwatch.amazonaws.com", "events.amazonaws.com"]
}

resource "aws_cloudwatch_event_rule" "ecs_failure" {
  name        = "${local.name}-ecs-failure"
  description = "ECS task 시작·배치 실패, 배포 실패"
  event_pattern = jsonencode({
    source      = ["aws.ecs"]
    detail-type = ["ECS Service Action", "ECS Deployment State Change"]
    resources   = [aws_ecs_service.app.id, aws_ecs_service.alloy.id]
    detail = {
      eventName = ["SERVICE_TASK_START_IMPAIRED", "SERVICE_TASK_PLACEMENT_FAILURE", "SERVICE_DEPLOYMENT_FAILED"]
    }
  })
}

resource "aws_cloudwatch_event_target" "ecs_failure" {
  rule = aws_cloudwatch_event_rule.ecs_failure.name
  arn  = module.alerts.topic_arn
}

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
    how   = ["ECS Exec으로 접속해 큰 테이블 확인", "Terraform `allocated_storage`로 저장 공간 확장"]
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

resource "aws_cloudwatch_metric_alarm" "db_cpu_surplus" {
  alarm_name = "${local.name}-db-cpu-surplus"
  alarm_description = jsonencode({
    title = "DB CPU credits exhausted (${aws_db_instance.this.identifier})"
    what  = "RDS CPU 크레딧을 다 쓰고 빌려 쓴 크레딧 {value} ({condition}, 15분 연속), 24시간 안에 못 갚으면 추가 요금 발생"
    why   = ["느린 쿼리", "트래픽 증가", "인스턴스(${aws_db_instance.this.instance_class}) 성능 부족"]
    how   = ["Grafana deepple-api 대시보드에서 slow query 확인", "Terraform `db.instance_class`로 인스턴스 변경"]
  })
  namespace           = "AWS/RDS"
  metric_name         = "CPUSurplusCreditBalance"
  dimensions          = { DBInstanceIdentifier = aws_db_instance.this.identifier }
  statistic           = "Maximum"
  period              = 300
  evaluation_periods  = 3
  threshold           = 0
  comparison_operator = "GreaterThanThreshold"
  treat_missing_data  = "notBreaching"
  alarm_actions       = [module.alerts.topic_arn]
  ok_actions          = [module.alerts.topic_arn]
}

resource "aws_cloudwatch_metric_alarm" "app_no_healthy_targets" {
  alarm_name = "${local.name}-app-no-healthy-targets"
  alarm_description = jsonencode({
    title = "No healthy targets (${aws_ecs_service.app.name})"
    what  = "ALB 정상 target {value}개 ({condition}, 3분 연속), API 전체 장애"
    why   = ["배포 후 앱 시작 실패", "DB·캐시 연결 실패", "Spot task 동시 회수"]
    how   = ["ECS 서비스 이벤트와 CloudWatch `${aws_cloudwatch_log_group.app.name}` 로그 확인", "최근 배포 확인"]
  })
  namespace           = "AWS/ApplicationELB"
  metric_name         = "HealthyHostCount"
  dimensions          = { LoadBalancer = aws_lb.this.arn_suffix, TargetGroup = aws_lb_target_group.app.arn_suffix }
  statistic           = "Minimum"
  period              = 60
  evaluation_periods  = 3
  threshold           = 1
  comparison_operator = "LessThanThreshold"
  treat_missing_data  = "breaching"
  alarm_actions       = [module.alerts.topic_arn]
  ok_actions          = [module.alerts.topic_arn]
}

resource "aws_cloudwatch_metric_alarm" "cache_storage" {
  alarm_name = "${local.name}-cache-storage"
  alarm_description = jsonencode({
    title = "High cache usage (${aws_elasticache_serverless_cache.this.name})"
    what  = "캐시 사용량 {value} ({condition}, 상한 ${aws_elasticache_serverless_cache.this.cache_usage_limits[0].data_storage[0].maximum}GB)"
    why   = ["TTL 없는 키 누적", "트래픽 증가"]
    how   = ["ECS Exec으로 접속해 큰 키 확인", "Terraform `cache_usage_limits`로 상한 변경"]
    unit  = "bytes"
  })
  namespace           = "AWS/ElastiCache"
  metric_name         = "BytesUsedForCache"
  dimensions          = { clusterId = aws_elasticache_serverless_cache.this.name }
  statistic           = "Maximum"
  period              = 300
  evaluation_periods  = 1
  threshold           = 1024 * 1024 * 1024
  comparison_operator = "GreaterThanThreshold"
  treat_missing_data  = "notBreaching"
  alarm_actions       = [module.alerts.topic_arn]
  ok_actions          = [module.alerts.topic_arn]
}

data "aws_sns_topic" "account_alerts" {
  name = "deepple-alerts"
}

resource "aws_cloudwatch_metric_alarm" "alerts_lambda_errors" {
  alarm_name = "${local.name}-alerts-lambda-errors"
  alarm_description = jsonencode({
    title = "Alert delivery failing (${module.alerts.function_name})"
    what  = "#alerts-prod 알림 Lambda 오류 {value} (5분간), 그동안의 prod 알림 유실 가능"
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
