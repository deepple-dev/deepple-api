resource "aws_elasticache_serverless_cache" "this" {
  name                 = local.name
  engine               = "valkey"
  major_engine_version = "8"
  subnet_ids           = module.vpc.private_subnets
  security_group_ids   = [aws_security_group.cache.id]

  cache_usage_limits {
    data_storage {
      maximum = 2
      unit    = "GB"
    }
  }

  daily_snapshot_time      = "18:00"
  snapshot_retention_limit = 3
}
