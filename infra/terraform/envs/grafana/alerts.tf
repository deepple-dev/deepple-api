resource "grafana_folder" "deepple" {
  title = "deepple"
}

locals {
  requests = "http_server_requests_milliseconds_count{deployment_environment_name=\"ENV\"}"

  alert_rules = {
    high_error_rate = {
      title     = "High error rate"
      severity  = "critical"
      query     = "sum(rate(http_server_requests_milliseconds_count{deployment_environment_name=\"ENV\", outcome=\"SERVER_ERROR\"}[5m])) / sum(rate(${local.requests}[5m])) and on() sum(increase(${local.requests}[5m])) >= 10"
      evaluator = { type = "gt", value = 0.05 }
      for       = "1m"
      no_data   = "OK"
      what      = "5분간 5xx 응답 비율 {{ humanizePercentage $values.A.Value }} (> 5%, 요청 10건 이상일 때)"
      why       = "- 처리 못 한 예외\n- DB·Redis·외부 API 장애"
      how       = "- Sentry에서 같은 시각 이슈 확인\n- Grafana에서 ERROR 로그와 trace 확인"
    }
    high_latency = {
      title     = "High latency"
      severity  = "critical"
      query     = "histogram_quantile(0.95, sum by (le) (rate(http_server_requests_milliseconds_bucket{deployment_environment_name=\"ENV\"}[5m])))"
      evaluator = { type = "gt", value = 1000 }
      for       = "10m"
      no_data   = "OK"
      what      = "응답 시간 p95 {{ printf \"%.0f\" $values.A.Value }}ms (> 1000ms, 10분 지속)"
      why       = "- 느린 DB 쿼리·커넥션 부족\n- 외부 API 지연\n- GC 증가"
      how       = "- Application Observability에서 느린 엔드포인트와 trace 확인\n- deepple-api 대시보드에서 DB 커넥션·GC 확인"
    }
    metrics_missing = {
      title     = "Metrics missing"
      severity  = "critical"
      query     = "count(target_info{deployment_environment_name=\"ENV\", service_name=\"deepple-api\"})"
      evaluator = { type = "lt", value = 1 }
      for       = "5m"
      no_data   = "Alerting"
      what      = "deepple-api 지표가 5분 넘게 들어오지 않음"
      why       = "- 앱 task 중지·재시작 반복\n- Alloy 중지\n- Grafana Cloud 전송 실패"
      how       = "- ECS 서비스(앱·Alloy) 상태 확인\n- CloudWatch(/ecs/deepple-ENV)에서 앱·Alloy 로그 확인"
    }
    db_connection_pending = {
      title     = "DB connections pending"
      severity  = "warning"
      query     = "max(hikaricp_connections_pending{deployment_environment_name=\"ENV\"})"
      evaluator = { type = "gt", value = 0 }
      for       = "5m"
      no_data   = "OK"
      what      = "DB 커넥션을 기다리는 요청 {{ printf \"%.0f\" $values.A.Value }}개 (5분 지속)"
      why       = "- 느린 쿼리로 커넥션 반환 지연\n- 트래픽 증가 대비 풀 크기 부족"
      how       = "- deepple-api 대시보드에서 활성 커넥션 확인\n- 느린 쿼리 trace 확인"
    }
    old_gen_usage = {
      title     = "High old gen usage after GC"
      severity  = "warning"
      query     = "max(jvm_gc_live_data_size_bytes{deployment_environment_name=\"ENV\"} / jvm_gc_max_data_size_bytes{deployment_environment_name=\"ENV\"})"
      evaluator = { type = "gt", value = 0.9 }
      for       = "15m"
      no_data   = "OK"
      what      = "GC 후 old 영역 사용률 {{ humanizePercentage $values.A.Value }} (> 90%, 15분 지속)"
      why       = "- 메모리 누수\n- 힙 크기 부족"
      how       = "- deepple-api 대시보드에서 힙 추이 확인\n- 최근 배포 확인"
    }
  }
}

resource "grafana_rule_group" "deepple_api" {
  for_each = var.environments

  name             = "deepple-api-${each.key}"
  folder_uid       = grafana_folder.deepple.uid
  interval_seconds = 60

  dynamic "rule" {
    for_each = local.alert_rules

    content {
      name           = rule.value.title
      condition      = "B"
      for            = rule.value.for
      no_data_state  = rule.value.no_data
      exec_err_state = "Error"

      labels = {
        env      = each.key
        severity = rule.value.severity
      }

      annotations = {
        title   = rule.value.title
        what    = replace(rule.value.what, "ENV", each.key)
        why     = rule.value.why
        how     = replace(rule.value.how, "ENV", each.key)
        mention = rule.value.severity == "critical" ? "<@&${each.value.mention_role_id}>" : ""
      }

      data {
        ref_id         = "A"
        datasource_uid = var.prometheus_datasource_uid

        relative_time_range {
          from = 600
          to   = 0
        }

        model = jsonencode({
          refId   = "A"
          expr    = replace(rule.value.query, "ENV", each.key)
          instant = true
        })
      }

      data {
        ref_id         = "B"
        datasource_uid = "__expr__"

        relative_time_range {
          from = 0
          to   = 0
        }

        model = jsonencode({
          refId      = "B"
          type       = "threshold"
          expression = "A"
          conditions = [{ evaluator = { type = rule.value.evaluator.type, params = [rule.value.evaluator.value] } }]
        })
      }
    }
  }
}
