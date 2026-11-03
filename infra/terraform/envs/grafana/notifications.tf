resource "grafana_message_template" "deepple" {
  name     = "deepple"
  template = file("${path.module}/templates/deepple.tmpl")
}

resource "grafana_mute_timing" "quiet_hours" {
  for_each = { for env, config in var.environments : env => config.quiet_hours if config.quiet_hours != null }

  name = "${each.key}-quiet-hours"

  intervals {
    location = "Asia/Seoul"
    times {
      start = each.value.start
      end   = each.value.end
    }
  }
}

resource "grafana_notification_policy" "this" {
  contact_point = var.environments["dev"].contact_point
  group_by      = ["env"]

  dynamic "policy" {
    for_each = var.environments

    content {
      contact_point = policy.value.contact_point
      group_by      = ["env"]
      mute_timings  = policy.value.quiet_hours != null ? [grafana_mute_timing.quiet_hours[policy.key].name] : []

      matcher {
        label = "env"
        match = "="
        value = policy.key
      }
    }
  }
}
