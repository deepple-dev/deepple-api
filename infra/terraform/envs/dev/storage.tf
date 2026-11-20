resource "aws_s3_bucket" "storage" {
  bucket = "${local.name}-storage"
}

resource "aws_s3_bucket_ownership_controls" "storage" {
  bucket = aws_s3_bucket.storage.id

  rule {
    object_ownership = "BucketOwnerEnforced"
  }
}

# 앱이 저장한 이미지 주소를 그대로 열 수 있게 읽기만 공개한다
resource "aws_s3_bucket_public_access_block" "storage" {
  bucket = aws_s3_bucket.storage.id

  block_public_acls       = true
  ignore_public_acls      = true
  block_public_policy     = false
  restrict_public_buckets = false
}

data "aws_iam_policy_document" "storage" {
  statement {
    sid       = "PublicRead"
    actions   = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.storage.arn}/*"]

    principals {
      type        = "*"
      identifiers = ["*"]
    }
  }

  statement {
    sid       = "DenyAnonymousImageUploadRead"
    effect    = "Deny"
    actions   = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.storage.arn}/tmp/*"]

    principals {
      type        = "*"
      identifiers = ["*"]
    }

    condition {
      test     = "StringNotEquals"
      variable = "aws:PrincipalAccount"
      values   = [var.account_id]
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

resource "aws_s3_bucket_lifecycle_configuration" "storage" {
  bucket = aws_s3_bucket.storage.id

  rule {
    id     = "expire-image-uploads"
    status = "Enabled"

    filter {
      prefix = "tmp/"
    }

    expiration {
      days = 7
    }
  }
}

resource "aws_s3_bucket_cors_configuration" "storage" {
  bucket = aws_s3_bucket.storage.id

  cors_rule {
    allowed_methods = ["PUT"]
    allowed_origins = ["https://admin.deepple.co.kr"]
    allowed_headers = ["content-type"]
    max_age_seconds = 3000
  }
}
