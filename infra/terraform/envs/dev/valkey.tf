# 앱 배포·야간 중지와 상관없이 켜 둔다. 이 task가 바뀔 때만 데이터가 사라진다
resource "aws_service_discovery_service" "valkey" {
  name = "valkey"

  dns_config {
    namespace_id   = aws_service_discovery_private_dns_namespace.this.id
    routing_policy = "MULTIVALUE"

    dns_records {
      type = "A"
      ttl  = 10
    }
  }
}

locals {
  valkey_host = "${aws_service_discovery_service.valkey.name}.${aws_service_discovery_private_dns_namespace.this.name}"
}

resource "aws_ecs_task_definition" "valkey" {
  family                   = "${local.name}-valkey"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = 256
  memory                   = 512
  execution_role_arn       = aws_iam_role.execution.arn

  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "ARM64"
  }

  container_definitions = jsonencode([
    {
      name      = "valkey"
      image     = "public.ecr.aws/valkey/valkey:8.1.11"
      essential = true

      portMappings = [{ containerPort = 6379, protocol = "tcp" }]

      healthCheck = {
        command  = ["CMD", "valkey-cli", "ping"]
        interval = 10
        timeout  = 3
        retries  = 3
      }

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.app.name
          awslogs-region        = var.region
          awslogs-stream-prefix = "valkey"
        }
      }
    },
  ])
}

resource "aws_ecs_service" "valkey" {
  name            = "${local.name}-valkey"
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.valkey.arn
  desired_count   = 1

  # 새 task를 먼저 띄우면 잠깐 두 개가 떠 데이터가 갈라지므로 기존 task를 먼저 내린다
  deployment_minimum_healthy_percent = 0
  deployment_maximum_percent         = 100

  capacity_provider_strategy {
    capacity_provider = "FARGATE_SPOT"
    weight            = 1
  }

  network_configuration {
    subnets          = module.vpc.public_subnets
    security_groups  = [aws_security_group.valkey.id]
    assign_public_ip = true
  }

  service_registries {
    registry_arn = aws_service_discovery_service.valkey.arn
  }

  depends_on = [aws_ecs_cluster_capacity_providers.this]
}
