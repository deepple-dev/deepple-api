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

# 태그 없는 것 삭제 규칙은 두지 않는다. 증명(태그 없음)이 지워지고, 증명은 이미지와 함께 ECR이 정리한다
data "aws_ecr_lifecycle_policy_document" "this" {
  rule {
    priority    = 1
    description = "최근 이미지 30개만 보관"

    selection {
      tag_status       = "tagged"
      tag_pattern_list = ["*"]
      count_type       = "imageCountMoreThan"
      count_number     = 30
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
