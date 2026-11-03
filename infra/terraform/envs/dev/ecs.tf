resource "aws_ecs_cluster" "this" {
  name = local.name
}

resource "aws_ecs_cluster_capacity_providers" "this" {
  cluster_name       = aws_ecs_cluster.this.name
  capacity_providers = ["FARGATE", "FARGATE_SPOT"]
}

resource "aws_cloudwatch_log_group" "app" {
  name              = "/ecs/${local.name}"
  retention_in_days = 14
}

data "aws_iam_policy_document" "ecs_tasks_trust" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }

    condition {
      test     = "StringEquals"
      variable = "aws:SourceAccount"
      values   = [var.account_id]
    }
  }
}

resource "aws_iam_role" "execution" {
  name               = "${local.name}-execution"
  description        = "Pull images and read secrets for deepple-dev tasks"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_trust.json
}

resource "aws_iam_role_policy_attachment" "execution" {
  role       = aws_iam_role.execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

data "aws_iam_policy_document" "execution_secrets" {
  statement {
    actions   = ["secretsmanager:GetSecretValue"]
    resources = [aws_secretsmanager_secret.app.arn, aws_secretsmanager_secret.external.arn, aws_secretsmanager_secret.grafana_cloud.arn]
  }

  statement {
    actions   = ["ssm:GetParameters"]
    resources = [aws_ssm_parameter.alloy_config.arn]
  }

  statement {
    actions   = ["ecr:CreateRepository", "ecr:BatchImportUpstreamImage"]
    resources = ["arn:aws:ecr:${var.region}:${var.account_id}:repository/docker-hub/*"]
  }
}

resource "aws_iam_role_policy" "execution_secrets" {
  name   = "read-secrets"
  role   = aws_iam_role.execution.id
  policy = data.aws_iam_policy_document.execution_secrets.json
}

# 이름을 바꾸면 Google Workload Identity 공급자 조건도 바꿔야 한다
resource "aws_iam_role" "app" {
  name               = "${local.name}-app"
  description        = "Application role for deepple-dev tasks"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_trust.json
}

data "aws_iam_policy_document" "app" {
  statement {
    sid       = "Storage"
    actions   = ["s3:GetObject", "s3:PutObject", "s3:DeleteObject"]
    resources = ["${aws_s3_bucket.storage.arn}/*"]
  }

  statement {
    sid = "EcsExec"
    actions = [
      "ssmmessages:CreateControlChannel",
      "ssmmessages:CreateDataChannel",
      "ssmmessages:OpenControlChannel",
      "ssmmessages:OpenDataChannel",
    ]
    resources = ["*"]
  }
}

resource "aws_iam_role_policy" "app" {
  name   = "app"
  role   = aws_iam_role.app.id
  policy = data.aws_iam_policy_document.app.json
}

locals {
  app_environment = {
    SPRING_PROFILES_ACTIVE      = "dev"
    MYSQL_HOST                  = aws_db_instance.this.address
    MYSQL_PORT                  = tostring(aws_db_instance.this.port)
    MYSQL_DATABASE              = aws_db_instance.this.db_name
    MYSQL_USER                  = aws_db_instance.this.username
    REDIS_HOST                  = local.valkey_host
    OTEL_EXPORTER_OTLP_ENDPOINT = "http://${local.alloy_host}:4318"
    AWS_S3_BUCKET_NAME          = aws_s3_bucket.storage.bucket
    AUTH_PREFIX_CODE            = "008008"

    GOOGLE_WORKLOAD_IDENTITY_AUDIENCE = "//iam.googleapis.com/${var.gcp_workload_identity_provider}"
    FIREBASE_PROJECT_ID               = var.firebase_project_id
    FIREBASE_SERVICE_ACCOUNT          = var.firebase_service_account
    GOOGLE_PLAY_SERVICE_ACCOUNT       = var.google_play_service_account
  }

  app_secrets = merge(
    {
      for key in ["JWT_SECRET", "MYSQL_PASSWORD"] :
      key => "${aws_secretsmanager_secret.app.arn}:${key}::"
    },
    {
      for key in local.external_secret_keys :
      key => "${aws_secretsmanager_secret.external.arn}:${key}::"
    },
  )
}

resource "aws_ecs_task_definition" "app" {
  family                   = local.name
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = 512
  memory                   = 2048
  execution_role_arn       = aws_iam_role.execution.arn
  task_role_arn            = aws_iam_role.app.arn

  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "X86_64"
  }

  container_definitions = jsonencode([
    {
      name      = "app"
      image     = "${data.aws_ecr_repository.app.repository_url}:${var.image_tag}"
      essential = true

      portMappings = [{ containerPort = 8080, protocol = "tcp" }]

      environment = [for name, value in local.app_environment : { name = name, value = value }]
      secrets     = [for name, arn in local.app_secrets : { name = name, valueFrom = arn }]

      # graceful shutdown 30초보다 여유 있게
      stopTimeout = 40

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.app.name
          awslogs-region        = var.region
          awslogs-stream-prefix = "app"
        }
      }
    },
  ])
}

resource "aws_ecs_service" "app" {
  name            = "${local.name}-api"
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = 1

  enable_execute_command            = true
  health_check_grace_period_seconds = 180

  # 태스크가 하나라 새 태스크가 뜬 뒤 기존 태스크를 내린다
  deployment_minimum_healthy_percent = 100
  deployment_maximum_percent         = 200

  capacity_provider_strategy {
    capacity_provider = "FARGATE_SPOT"
    weight            = 1
  }

  network_configuration {
    subnets          = module.vpc.public_subnets
    security_groups  = [aws_security_group.app.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.app.arn
    container_name   = "app"
    container_port   = 8080
  }

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  # 배포는 CD가, 태스크 수는 야간 중지 스케줄이 바꾼다
  lifecycle {
    ignore_changes = [task_definition, desired_count]
  }

  depends_on = [aws_lb_listener.https, aws_ecs_cluster_capacity_providers.this]
}
