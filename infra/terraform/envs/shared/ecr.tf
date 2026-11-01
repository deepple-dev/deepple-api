resource "aws_ecr_repository" "this" {
  name                 = "deepple-api"
  image_tag_mutability = "IMMUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  encryption_configuration {
    encryption_type = "AES256"
  }
}

data "aws_ecr_lifecycle_policy_document" "this" {
  rule {
    priority    = 1
    description = "태그 없는 이미지는 7일 뒤 삭제"

    selection {
      tag_status   = "untagged"
      count_type   = "sinceImagePushed"
      count_unit   = "days"
      count_number = 7
    }

    action {
      type = "expire"
    }
  }

  rule {
    priority    = 2
    description = "최근 이미지 30개만 보관"

    selection {
      tag_status   = "any"
      count_type   = "imageCountMoreThan"
      count_number = 30
    }

    action {
      type = "expire"
    }
  }
}

resource "aws_ecr_lifecycle_policy" "this" {
  repository = aws_ecr_repository.this.name
  policy     = data.aws_ecr_lifecycle_policy_document.this.json
}
