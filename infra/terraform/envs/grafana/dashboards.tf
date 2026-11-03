locals {
  datasource = { type = "prometheus", uid = var.prometheus_datasource_uid }
  env        = "deployment_environment_name=\"$env\""

  panels = [
    { title = "요청 수 (초당)", unit = "reqps", expr = "sum by (outcome) (rate(http_server_requests_milliseconds_count{${local.env}}[$__rate_interval]))", legend = "{{outcome}}" },
    { title = "5xx 비율", unit = "percentunit", expr = "sum(rate(http_server_requests_milliseconds_count{${local.env}, outcome=\"SERVER_ERROR\"}[$__rate_interval])) / sum(rate(http_server_requests_milliseconds_count{${local.env}}[$__rate_interval]))", legend = "5xx" },
    { title = "응답 시간 p95", unit = "ms", expr = "histogram_quantile(0.95, sum by (le) (rate(http_server_requests_milliseconds_bucket{${local.env}}[$__rate_interval])))", legend = "p95" },
    { title = "엔드포인트별 응답 시간 p95", unit = "ms", expr = "histogram_quantile(0.95, sum by (le, uri) (rate(http_server_requests_milliseconds_bucket{${local.env}}[$__rate_interval])))", legend = "{{uri}}" },
    { title = "DB 커넥션", unit = "short", expr = "sum by (__name__) ({__name__=~\"hikaricp_connections_(active|idle|pending)\", ${local.env}})", legend = "{{__name__}}" },
    { title = "GC 후 old 영역 사용률", unit = "percentunit", expr = "max(jvm_gc_live_data_size_bytes{${local.env}} / jvm_gc_max_data_size_bytes{${local.env}})", legend = "old gen" },
    { title = "JVM 힙", unit = "bytes", expr = "sum(jvm_memory_used_bytes{${local.env}, area=\"heap\"})", legend = "used" },
    { title = "GC 시간 비율", unit = "percentunit", expr = "sum(rate(jvm_gc_pause_milliseconds_sum{${local.env}}[$__rate_interval])) / 1000", legend = "gc" },
  ]
}

resource "grafana_dashboard" "deepple_api" {
  folder = grafana_folder.deepple.uid

  config_json = jsonencode({
    title         = "deepple-api"
    uid           = "deepple-api"
    timezone      = "Asia/Seoul"
    schemaVersion = 39
    time          = { from = "now-6h", to = "now" }

    templating = {
      list = [{
        name       = "env"
        label      = "환경"
        type       = "query"
        datasource = local.datasource
        query      = { query = "label_values(target_info{service_name=\"deepple-api\"}, deployment_environment_name)", refId = "env" }
        refresh    = 2
        current    = { text = "dev", value = "dev" }
      }]
    }

    panels = [
      for index, panel in local.panels : {
        id         = index + 1
        type       = "timeseries"
        title      = panel.title
        datasource = local.datasource
        gridPos    = { x = (index % 2) * 12, y = floor(index / 2) * 8, w = 12, h = 8 }
        fieldConfig = {
          defaults  = { unit = panel.unit }
          overrides = []
        }
        targets = [{ refId = "A", datasource = local.datasource, expr = panel.expr, legendFormat = panel.legend }]
      }
    ]
  })
}
