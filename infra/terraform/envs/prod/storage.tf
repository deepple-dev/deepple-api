resource "aws_s3_bucket" "storage" {
  bucket = "${local.name}-storage"
}

resource "aws_s3_bucket_ownership_controls" "storage" {
  bucket = aws_s3_bucket.storage.id

  rule {
    object_ownership = "BucketOwnerEnforced"
  }
}

resource "aws_s3_bucket_public_access_block" "storage" {
  bucket = aws_s3_bucket.storage.id

  block_public_acls       = true
  ignore_public_acls      = true
  block_public_policy     = true
  restrict_public_buckets = true
}

data "aws_iam_policy_document" "storage" {
  statement {
    sid       = "CloudFrontRead"
    actions   = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.storage.arn}/*"]

    principals {
      type        = "Service"
      identifiers = ["cloudfront.amazonaws.com"]
    }

    condition {
      test     = "StringEquals"
      variable = "AWS:SourceArn"
      values   = [aws_cloudfront_distribution.images.arn]
    }
  }

  statement {
    sid       = "DenyInsecureTransport"
    effect    = "Deny"
    actions   = ["s3:*"]
    resources = [aws_s3_bucket.storage.arn, "${aws_s3_bucket.storage.arn}/*"]

    principals {
      type        = "*"
      identifiers = ["*"]
    }

    condition {
      test     = "Bool"
      variable = "aws:SecureTransport"
      values   = ["false"]
    }
  }
}

resource "aws_s3_bucket_policy" "storage" {
  bucket = aws_s3_bucket.storage.id
  policy = data.aws_iam_policy_document.storage.json

  depends_on = [aws_s3_bucket_public_access_block.storage]
}

resource "aws_acm_certificate" "images" {
  provider = aws.us_east_1

  domain_name       = var.images_domain
  validation_method = "DNS"

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_route53_record" "images_cert_validation" {
  for_each = {
    for option in aws_acm_certificate.images.domain_validation_options : option.domain_name => option
  }

  zone_id = data.aws_route53_zone.this.zone_id
  name    = each.value.resource_record_name
  type    = each.value.resource_record_type
  ttl     = 300
  records = [each.value.resource_record_value]
}

resource "aws_acm_certificate_validation" "images" {
  provider = aws.us_east_1

  certificate_arn         = aws_acm_certificate.images.arn
  validation_record_fqdns = [for record in aws_route53_record.images_cert_validation : record.fqdn]
}

resource "aws_cloudfront_origin_access_control" "storage" {
  name                              = "${local.name}-storage"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

data "aws_cloudfront_cache_policy" "caching_optimized" {
  name = "Managed-CachingOptimized"
}

resource "aws_cloudfront_distribution" "images" {
  enabled         = true
  is_ipv6_enabled = true
  aliases         = [var.images_domain]
  price_class     = "PriceClass_200"

  origin {
    origin_id                = "storage"
    domain_name              = aws_s3_bucket.storage.bucket_regional_domain_name
    origin_access_control_id = aws_cloudfront_origin_access_control.storage.id
  }

  default_cache_behavior {
    target_origin_id       = "storage"
    viewer_protocol_policy = "redirect-to-https"
    allowed_methods        = ["GET", "HEAD"]
    cached_methods         = ["GET", "HEAD"]
    cache_policy_id        = data.aws_cloudfront_cache_policy.caching_optimized.id
    compress               = true
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn      = aws_acm_certificate_validation.images.certificate_arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2021"
  }
}

resource "aws_route53_record" "images" {
  for_each = toset(["A", "AAAA"])

  zone_id = data.aws_route53_zone.this.zone_id
  name    = var.images_domain
  type    = each.value

  alias {
    name                   = aws_cloudfront_distribution.images.domain_name
    zone_id                = aws_cloudfront_distribution.images.hosted_zone_id
    evaluate_target_health = false
  }
}
