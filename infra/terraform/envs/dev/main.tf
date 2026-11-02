locals {
  name = "deepple-dev"
}

data "aws_route53_zone" "this" {
  name = "deepple.co.kr"
}

data "aws_ecr_repository" "app" {
  name = "deepple-api"
}
