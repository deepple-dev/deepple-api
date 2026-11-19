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

resource "aws_cloudwatch_metric_alarm" "kafka_status_check" {
  alarm_name = "${local.name}-kafka-status-check"
  alarm_description = jsonencode({
    title = "Kafka instance status check failed (${aws_instance.kafka.id})"
    what  = "Kafka EC2 상태 검사 실패 {value} ({condition}, 2분 연속), 그동안 발행은 event_publication에 쌓이고 소비가 멈춤"
    why   = ["AWS 하드웨어 장애(자동 복구 시도)", "OS 멈춤·메모리 부족", "부팅 실패"]
    how   = ["EC2 콘솔에서 시스템·인스턴스 상태 검사 확인", "Session Manager로 접속해 `docker ps`, `docker logs kafka` 확인", "인스턴스 재부팅 또는 `terraform apply -replace=aws_instance.kafka`"]
  })
  namespace           = "AWS/EC2"
  metric_name         = "StatusCheckFailed"
  dimensions          = { InstanceId = aws_instance.kafka.id }
  statistic           = "Maximum"
  period              = 60
  evaluation_periods  = 2
  threshold           = 1
  comparison_operator = "GreaterThanOrEqualToThreshold"
  treat_missing_data  = "breaching"
  alarm_actions       = [module.alerts.topic_arn]
  ok_actions          = [module.alerts.topic_arn]
}

resource "aws_cloudwatch_metric_alarm" "kafka_cpu_surplus" {
  alarm_name = "${local.name}-kafka-cpu-surplus"
  alarm_description = jsonencode({
    title = "Kafka CPU credits exhausted (${aws_instance.kafka.id})"
    what  = "Kafka EC2 CPU 크레딧을 다 쓰고 빌려 쓴 크레딧 {value} ({condition}, 15분 연속), 24시간 안에 못 갚으면 추가 요금 발생"
    why   = ["메시지·파티션 증가", "소비자 재처리로 읽기 급증", "인스턴스(${aws_instance.kafka.instance_type}) 성능 부족"]
    how   = ["CloudWatch `${aws_cloudwatch_log_group.kafka.name}` 로그 확인", "Terraform `kafka.instance_type`으로 인스턴스 변경"]
  })
  namespace           = "AWS/EC2"
  metric_name         = "CPUSurplusCreditBalance"
  dimensions          = { InstanceId = aws_instance.kafka.id }
  statistic           = "Maximum"
  period              = 300
  evaluation_periods  = 3
  threshold           = 0
  comparison_operator = "GreaterThanThreshold"
  treat_missing_data  = "notBreaching"
  alarm_actions       = [module.alerts.topic_arn]
  ok_actions          = [module.alerts.topic_arn]
}

resource "aws_cloudwatch_metric_alarm" "kafka_data_disk" {
  alarm_name = "${local.name}-kafka-data-disk"
  alarm_description = jsonencode({
    title = "High Kafka data disk usage (${aws_instance.kafka.id})"
    what  = "Kafka 데이터 디스크 사용률 {value} ({condition}, 15분 연속, 전체 ${aws_ebs_volume.kafka.size}GB), 가득 차면 브로커가 멈춤. 값이 15분간 안 와도 울림"
    why   = ["메시지 증가", "보존 기간 설정 변경", "CloudWatch agent 멈춤(값이 안 옴)"]
    how   = ["Session Manager로 접속해 `df -h ${local.kafka_data_dir}`, `du -sh ${local.kafka_data_dir}/*` 확인", "Terraform `kafka.volume_size`로 EBS를 늘린 뒤 `xfs_growfs ${local.kafka_data_dir}`", "agent 상태 `amazon-cloudwatch-agent-ctl -a status` 확인"]
    unit  = "percent"
  })
  namespace           = "CWAgent"
  metric_name         = "disk_used_percent"
  dimensions          = { InstanceId = aws_instance.kafka.id, path = local.kafka_data_dir, fstype = "xfs" }
  statistic           = "Maximum"
  period              = 300
  evaluation_periods  = 3
  threshold           = 80
  comparison_operator = "GreaterThanThreshold"
  treat_missing_data  = "breaching"
  alarm_actions       = [module.alerts.topic_arn]
  ok_actions          = [module.alerts.topic_arn]
}

resource "aws_cloudwatch_metric_alarm" "kafka_root_disk" {
  alarm_name = "${local.name}-kafka-root-disk"
  alarm_description = jsonencode({
    title = "High Kafka root disk usage (${aws_instance.kafka.id})"
    what  = "Kafka EC2 루트 디스크 사용률 {value} ({condition})"
    why   = ["Docker 이미지 누적", "OS 로그·패키지 캐시 증가"]
    how   = ["Session Manager로 접속해 `df -h /`, `docker system df` 확인", "`docker image prune`, `dnf clean all`로 정리"]
    unit  = "percent"
  })
  namespace           = "CWAgent"
  metric_name         = "disk_used_percent"
  dimensions          = { InstanceId = aws_instance.kafka.id, path = "/", fstype = "xfs" }
  statistic           = "Maximum"
  period              = 300
  evaluation_periods  = 1
  threshold           = 80
  comparison_operator = "GreaterThanThreshold"
  treat_missing_data  = "notBreaching"
  alarm_actions       = [module.alerts.topic_arn]
  ok_actions          = [module.alerts.topic_arn]
}

resource "aws_cloudwatch_metric_alarm" "kafka_memory" {
  alarm_name = "${local.name}-kafka-memory"
  alarm_description = jsonencode({
    title = "High Kafka memory usage (${aws_instance.kafka.id})"
    what  = "Kafka EC2 메모리 사용률 {value} ({condition}, 10분 평균), swap이 없어 다 차면 OOM killer가 Kafka를 멈춤"
    why   = ["메시지·파티션 증가", "인스턴스(${aws_instance.kafka.instance_type}) 메모리 부족"]
    how   = ["Session Manager로 접속해 `free -m`, `docker stats kafka` 확인", "Terraform `kafka.instance_type`으로 인스턴스 변경"]
    unit  = "percent"
  })
  namespace           = "CWAgent"
  metric_name         = "mem_used_percent"
  dimensions          = { InstanceId = aws_instance.kafka.id }
  statistic           = "Average"
  period              = 300
  evaluation_periods  = 2
  threshold           = 90
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
