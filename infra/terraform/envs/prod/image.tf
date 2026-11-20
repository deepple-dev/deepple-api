module "image_processing" {
  source = "../../modules/image-processing"

  name            = local.name
  account_id      = var.account_id
  bucket_id       = aws_s3_bucket.storage.id
  bucket_arn      = aws_s3_bucket.storage.arn
  alarm_topic_arn = module.alerts.topic_arn
}
