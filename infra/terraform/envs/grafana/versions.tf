terraform {
  required_version = ">= 1.11"

  required_providers {
    grafana = {
      source  = "grafana/grafana"
      version = "~> 4.0"
    }
  }

  backend "s3" {
    bucket       = "deepple-terraform-state-339713092351"
    key          = "envs/grafana/terraform.tfstate"
    region       = "ap-northeast-2"
    encrypt      = true
    use_lockfile = true
  }
}

provider "grafana" {
  url = var.grafana_url
}
