locals {
  name = "deepple-prod"
}

data "aws_route53_zone" "this" {
  name = "deepple.co.kr"
}

resource "aws_service_discovery_private_dns_namespace" "this" {
  name = "${local.name}.local"
  vpc  = module.vpc.vpc_id
}
