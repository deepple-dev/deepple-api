output "vpc_id" {
  value = module.vpc.vpc_id
}

output "db_address" {
  value = aws_db_instance.this.address
}

output "cache_endpoint" {
  value = aws_elasticache_serverless_cache.this.endpoint[0].address
}

output "images_url" {
  value = "https://${var.images_domain}"
}
