resource "aws_db_parameter_group" "this" {
  name   = local.name
  family = "mysql8.4"

  parameter {
    name  = "time_zone"
    value = "Asia/Seoul"
  }
}

resource "aws_db_instance" "this" {
  identifier     = local.name
  engine         = "mysql"
  engine_version = "8.4"
  instance_class = "db.t4g.micro"

  allocated_storage = 20
  storage_type      = "gp3"
  storage_encrypted = true

  db_name             = "deepple"
  username            = "deepple"
  password_wo         = ephemeral.random_password.db.result
  password_wo_version = 1

  db_subnet_group_name   = module.vpc.database_subnet_group_name
  parameter_group_name   = aws_db_parameter_group.this.name
  vpc_security_group_ids = [aws_security_group.db.id]
  publicly_accessible    = false
  multi_az               = false

  # 야간 중지(KST 02~08시)를 피한다. 시간은 UTC
  backup_retention_period    = 7
  backup_window              = "15:00-15:30"
  maintenance_window         = "sun:15:30-sun:16:00"
  auto_minor_version_upgrade = true
  apply_immediately          = true

  deletion_protection = false
  skip_final_snapshot = true
}
