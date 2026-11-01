terraform {
  required_version = ">= 1.11"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
    archive = {
      source  = "hashicorp/archive"
      version = "~> 2.0"
    }
  }

  backend "s3" {
    bucket       = "deepple-terraform-state-339713092351"
    key          = "envs/shared/terraform.tfstate"
    region       = "ap-northeast-2"
    encrypt      = true
    use_lockfile = true
  }
}

provider "aws" {
  region = var.region

  allowed_account_ids = [var.account_id]

  default_tags {
    tags = {
      Project   = "deepple"
      Stack     = "shared"
      ManagedBy = "terraform"
    }
  }
}
