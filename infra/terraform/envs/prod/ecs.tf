resource "aws_ecs_cluster" "this" {
  name = local.name
}

resource "aws_ecs_cluster_capacity_providers" "this" {
  cluster_name       = aws_ecs_cluster.this.name
  capacity_providers = ["FARGATE", "FARGATE_SPOT"]
}

resource "aws_cloudwatch_log_group" "app" {
  name              = "/ecs/${local.name}"
  retention_in_days = 30
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
  description        = "Pull images and read secrets for deepple-prod tasks"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_trust.json
}

resource "aws_iam_role_policy_attachment" "execution" {
  role       = aws_iam_role.execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

data "aws_iam_policy_document" "execution_secrets" {
  statement {
    actions = ["secretsmanager:GetSecretValue"]
    resources = [
      aws_secretsmanager_secret.app.arn,
      aws_secretsmanager_secret.jwt.arn,
      aws_secretsmanager_secret.external.arn,
      aws_secretsmanager_secret.grafana_cloud.arn,
    ]
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

resource "aws_iam_role" "app" {
  name               = "${local.name}-app"
  description        = "Application role for deepple-prod tasks"
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
    SPRING_PROFILES_ACTIVE      = "prod"
    MYSQL_HOST                  = aws_db_instance.this.address
    MYSQL_PORT                  = tostring(aws_db_instance.this.port)
    MYSQL_DATABASE              = aws_db_instance.this.db_name
    MYSQL_USER                  = aws_db_instance.this.username
    REDIS_HOST                  = aws_elasticache_serverless_cache.this.endpoint[0].address
    OTEL_EXPORTER_OTLP_ENDPOINT = "http://${local.alloy_host}:4318"
    AWS_S3_BUCKET_NAME          = aws_s3_bucket.storage.bucket
    AUTH_PREFIX_CODE            = ""

    GOOGLE_WORKLOAD_IDENTITY_AUDIENCE = "//iam.googleapis.com/${var.gcp_workload_identity_provider}"
    FIREBASE_PROJECT_ID               = var.firebase_project_id
    FIREBASE_SERVICE_ACCOUNT          = var.firebase_service_account
    GOOGLE_PLAY_SERVICE_ACCOUNT       = var.google_play_service_account
  }

  app_secrets = merge(
    {
      for key in ["MYSQL_PASSWORD"] :
      key => "${aws_secretsmanager_secret.app.arn}:${key}::"
    },
    {
      JWT_PRIVATE_KEY = "${aws_secretsmanager_secret.jwt.arn}:JWT_PRIVATE_KEY::"
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
  cpu                      = var.app.cpu
  memory                   = var.app.memory
  execution_role_arn       = aws_iam_role.execution.arn
  task_role_arn            = aws_iam_role.app.arn

  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "ARM64"
  }

  container_definitions = jsonencode([
    {
      name      = "app"
      image     = "${data.aws_ecr_repository.app.repository_url}:${var.image_tag}"
      essential = true

      portMappings = [
        { containerPort = 8080, protocol = "tcp" },
        { containerPort = 8081, protocol = "tcp" },
      ]

      environment = [for name, value in local.app_environment : { name = name, value = value }]
      secrets     = [for name, arn in local.app_secrets : { name = name, valueFrom = arn }]

      stopTimeout = 40

      healthCheck = {
        command     = ["CMD-SHELL", "curl -fs http://localhost:8081/actuator/health/readiness || exit 1"]
        interval    = 15
        timeout     = 5
        retries     = 3
        startPeriod = 180
      }

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
  desired_count   = var.app.min_count

  enable_execute_command            = true
  health_check_grace_period_seconds = 180

  deployment_minimum_healthy_percent = 100
  deployment_maximum_percent         = 200

  capacity_provider_strategy {
    capacity_provider = "FARGATE"
    base              = var.app.on_demand_base
    weight            = 0
  }

  capacity_provider_strategy {
    capacity_provider = "FARGATE_SPOT"
    weight            = 1
  }

  network_configuration {
    subnets         = module.vpc.private_subnets
    security_groups = [aws_security_group.app.id]
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

  lifecycle {
    ignore_changes = [task_definition, desired_count]
  }

  depends_on = [aws_ecs_cluster_capacity_providers.this, aws_lb_listener.https]
}

resource "aws_appautoscaling_target" "app" {
  service_namespace  = "ecs"
  resource_id        = "service/${aws_ecs_cluster.this.name}/${aws_ecs_service.app.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  min_capacity       = var.app.min_count
  max_capacity       = var.app.max_count
}

resource "aws_appautoscaling_policy" "app_cpu" {
  name               = "${local.name}-cpu"
  policy_type        = "TargetTrackingScaling"
  service_namespace  = aws_appautoscaling_target.app.service_namespace
  resource_id        = aws_appautoscaling_target.app.resource_id
  scalable_dimension = aws_appautoscaling_target.app.scalable_dimension

  target_tracking_scaling_policy_configuration {
    target_value = var.app.cpu_target

    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
  }
}

data "aws_ecr_repository" "app" {
  name = "deepple-api"
}
