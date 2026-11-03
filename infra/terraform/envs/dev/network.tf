data "aws_availability_zones" "available" {
  state = "available"
}

# NAT 게이트웨이 없이 앱은 퍼블릭 서브넷에서 공인 IP로 외부와 통신한다
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 6.0"

  name = local.name
  cidr = "10.10.0.0/16"
  azs  = slice(data.aws_availability_zones.available.names, 0, 2)

  public_subnets   = ["10.10.0.0/24", "10.10.1.0/24"]
  database_subnets = ["10.10.10.0/24", "10.10.11.0/24"]

  create_database_subnet_group = true
  enable_nat_gateway           = false
}

resource "aws_security_group" "vpc_link" {
  name        = "${local.name}-vpc-link"
  description = "API Gateway VPC link"
  vpc_id      = module.vpc.vpc_id
}

resource "aws_vpc_security_group_egress_rule" "vpc_link_to_app" {
  security_group_id            = aws_security_group.vpc_link.id
  referenced_security_group_id = aws_security_group.app.id
  ip_protocol                  = "tcp"
  from_port                    = 8080
  to_port                      = 8080
}

resource "aws_security_group" "app" {
  name        = "${local.name}-app"
  description = "API tasks"
  vpc_id      = module.vpc.vpc_id
}

resource "aws_vpc_security_group_ingress_rule" "app_from_vpc_link" {
  security_group_id            = aws_security_group.app.id
  referenced_security_group_id = aws_security_group.vpc_link.id
  ip_protocol                  = "tcp"
  from_port                    = 8080
  to_port                      = 8080
}

resource "aws_vpc_security_group_egress_rule" "app_all" {
  security_group_id = aws_security_group.app.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

resource "aws_security_group" "db" {
  name        = "${local.name}-db"
  description = "RDS"
  vpc_id      = module.vpc.vpc_id
}

resource "aws_vpc_security_group_ingress_rule" "db_from_app" {
  security_group_id            = aws_security_group.db.id
  referenced_security_group_id = aws_security_group.app.id
  ip_protocol                  = "tcp"
  from_port                    = 3306
  to_port                      = 3306
}
