resource "grafana_folder" "deepple" {
  title = "deepple"
}

locals {
  user_requests   = "deployment_environment_name=\"ENV\", uri!~\"/v3/api-docs.*|/swagger-ui.*\""
  requests        = "http_server_requests_milliseconds_count{${local.user_requests}}"
  enough_requests = "and on() sum(increase(${local.requests}[5m])) >= 10"

  alert_rules = {
    high_error_rate = {
      title     = "High error rate"
      severity  = "critical"
      query     = "sum(rate(http_server_requests_milliseconds_count{${local.user_requests}, outcome=\"SERVER_ERROR\"}[5m])) / sum(rate(${local.requests}[5m])) ${local.enough_requests}"
      evaluator = { type = "gt", value = 0.05 }
      for       = "1m"
      no_data   = "OK"
      what      = "5xx 응답 비율 {{ humanizePercentage $values.A.Value }} (> 5%, 5분간, 요청 10건 이상일 때)"
      why       = "- 처리 못 한 예외\n- DB·Redis·외부 API 장애"
      how       = "- Sentry에서 같은 시각 이슈 확인\n- Grafana Explore에서 같은 시각 ERROR 로그·trace 확인"
    }
    high_latency = {
      title     = "High latency"
      severity  = "critical"
      query     = "histogram_quantile(0.95, sum by (le) (rate(http_server_requests_milliseconds_bucket{${local.user_requests}}[5m]))) ${local.enough_requests}"
      evaluator = { type = "gt", value = 1000 }
      for       = "10m"
      no_data   = "OK"
      what      = "응답 시간 p95 {{ printf \"%.0f\" $values.A.Value }}ms (> 1000ms, 10분 지속, 요청 10건 이상일 때)"
      why       = "- DB slow query·커넥션 부족\n- 외부 API 지연 (Bizgo, Google, Apple)\n- GC 증가"
      how       = "- deepple-api 대시보드에서 엔드포인트별 p95와 DB 커넥션·GC 확인\n- Application Observability에서 느린 trace 확인"
    }
    metrics_missing = {
      title     = "Metrics missing"
      severity  = "critical"
      query     = "count(target_info{deployment_environment_name=\"ENV\", service_name=\"deepple-api\"})"
      evaluator = { type = "lt", value = 1 }
      for       = "5m"
      no_data   = "Alerting"
      what      = "앱 지표가 5분 넘게 들어오지 않음"
      why       = "- 앱 task 중지·재시작 반복\n- Alloy 중지\n- Alloy → Grafana Cloud 전송 실패 (토큰 만료 등)"
      how       = "- ECS에서 앱·Alloy 서비스 상태 확인\n- CloudWatch `/ecs/deepple-ENV`에서 앱·Alloy 로그 확인"
    }
    db_connection_pending = {
      title     = "DB connections pending"
      severity  = "warning"
      query     = "max(hikaricp_connections_pending{deployment_environment_name=\"ENV\"})"
      evaluator = { type = "gt", value = 0 }
      for       = "5m"
      no_data   = "OK"
      what      = "DB 커넥션을 기다리는 요청 {{ printf \"%.0f\" $values.A.Value }}개 (> 0, 5분 지속)"
      why       = "- slow query로 커넥션 반환 지연\n- 트래픽 대비 커넥션 풀 크기 부족"
      how       = "- deepple-api 대시보드에서 활성·대기 커넥션 확인\n- Application Observability에서 slow query trace 확인"
    }
    old_gen_usage = {
      title     = "High old gen usage after GC"
      severity  = "warning"
      query     = "max(jvm_gc_live_data_size_bytes{deployment_environment_name=\"ENV\"} / jvm_gc_max_data_size_bytes{deployment_environment_name=\"ENV\"})"
      evaluator = { type = "gt", value = 0.9 }
      for       = "15m"
      no_data   = "OK"
      what      = "GC 후 old gen 사용률 {{ humanizePercentage $values.A.Value }} (> 90%, 15분 지속)"
      why       = "- 메모리 누수\n- heap 크기 부족"
      how       = "- deepple-api 대시보드에서 old gen·heap 추이 확인\n- 최근 배포 확인"
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
        service  = "deepple-${each.key}-api"
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
