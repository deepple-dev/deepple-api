variable "grafana_url" {
  description = "Grafana Cloud 스택 주소"
  type        = string
  default     = "https://deepple.grafana.net"
}

variable "prometheus_datasource_uid" {
  description = "알람·대시보드가 쓰는 Prometheus(Mimir) 데이터 소스 UID"
  type        = string
  default     = "grafanacloud-prom"
}

variable "environments" {
  description = "알람을 보낼 환경. contact_point(멘션용)와 card_contact_point(Show Message in Embed를 켠 경고용)는 Grafana에서 직접 만든 Discord 연락처 이름이고, quiet_hours는 야간 중지로 앱이 꺼져 있는 KST 시간대(HH:MM)"
  type = map(object({
    contact_point      = string
    card_contact_point = string
    mention_role_id    = string
    quiet_hours = optional(object({
      start = string
      end   = string
    }))
  }))
  default = {
    dev = {
      contact_point      = "discord-dev"
      card_contact_point = "discord-dev-card"
      mention_role_id    = "1394957566953652256"
      quiet_hours        = { start = "01:55", end = "08:20" }
    }
  }
}
