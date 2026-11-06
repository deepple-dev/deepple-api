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
  instance_class = var.db.instance_class

  allocated_storage     = var.db.allocated_storage
  max_allocated_storage = var.db.max_allocated_storage
  storage_type          = "gp3"
  storage_encrypted     = true

  db_name             = "deepple"
  username            = "deepple"
  password_wo         = ephemeral.random_password.db.result
  password_wo_version = 1

  db_subnet_group_name   = module.vpc.database_subnet_group_name
  parameter_group_name   = aws_db_parameter_group.this.name
  vpc_security_group_ids = [aws_security_group.db.id]
  publicly_accessible    = false
  multi_az               = var.db.multi_az

  backup_retention_period    = var.db.backup_retention_period
  backup_window              = "18:00-18:30"
  maintenance_window         = "sun:18:30-sun:19:00"
  auto_minor_version_upgrade = true

  deletion_protection       = true
  skip_final_snapshot       = false
  final_snapshot_identifier = "${local.name}-final"
  copy_tags_to_snapshot     = true
}
