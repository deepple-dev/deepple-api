locals {
  name = "deepple-prod"
}

data "aws_route53_zone" "this" {
  name = "deepple.co.kr"
}

data "aws_availability_zones" "available" {
  state = "available"
}
