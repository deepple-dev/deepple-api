# 같은 URL의 OIDC 공급자는 계정에 하나만 만들 수 있다
resource "aws_iam_openid_connect_provider" "github" {
  url            = "https://token.actions.githubusercontent.com"
  client_id_list = ["sts.amazonaws.com"]
}

data "aws_iam_policy_document" "github_image_push_trust" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [aws_iam_openid_connect_provider.github.arn]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"
      values   = ["sts.amazonaws.com"]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:sub"
      values   = ["repo:${var.github_repository}:ref:refs/heads/main"]
    }
  }
}

resource "aws_iam_role" "github_image_push" {
  name                 = "deepple-github-image-push"
  description          = "Push images to ECR deepple-api from GitHub Actions on main"
  assume_role_policy   = data.aws_iam_policy_document.github_image_push_trust.json
  max_session_duration = 3600
}

data "aws_iam_policy_document" "github_image_push" {
  statement {
    sid       = "EcrLogin"
    actions   = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }

  statement {
    sid = "EcrPush"
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:BatchGetImage",
      "ecr:CompleteLayerUpload",
      "ecr:DescribeImages",
      "ecr:GetDownloadUrlForLayer",
      "ecr:InitiateLayerUpload",
      "ecr:PutImage",
      "ecr:UploadLayerPart",
    ]
    resources = [aws_ecr_repository.this.arn]
  }
}

resource "aws_iam_role_policy" "github_image_push" {
  name   = "ecr-push"
  role   = aws_iam_role.github_image_push.id
  policy = data.aws_iam_policy_document.github_image_push.json
}
