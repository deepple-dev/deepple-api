resource "aws_service_discovery_service" "alloy" {
  name = "alloy"

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
  alloy_host = "${aws_service_discovery_service.alloy.name}.${aws_service_discovery_private_dns_namespace.this.name}"
}

resource "aws_ssm_parameter" "alloy_config" {
  name  = "/${local.name}/alloy/config"
  type  = "String"
  value = file("${path.module}/../../../alloy/config.alloy")
}

resource "aws_secretsmanager_secret" "grafana_cloud" {
  name        = "${local.name}/grafana-cloud"
  description = "prod Alloy가 Grafana Cloud로 보낼 때 쓰는 토큰"
}

resource "aws_ecs_task_definition" "alloy" {
  family                   = "${local.name}-alloy"
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
      name      = "alloy"
      image     = "${var.account_id}.dkr.ecr.${var.region}.amazonaws.com/docker-hub/grafana/alloy:${var.alloy_version}"
      essential = true

      portMappings = [
        { containerPort = 4317, protocol = "tcp" },
        { containerPort = 4318, protocol = "tcp" },
      ]

      entryPoint = ["/bin/sh", "-c"]
      command    = ["printenv ALLOY_CONFIG_CONTENT > /tmp/config_file && exec /bin/alloy run /tmp/config_file"]

      environment = [
        { name = "GRAFANA_CLOUD_OTLP_ENDPOINT", value = var.grafana_cloud_otlp_endpoint },
        { name = "GRAFANA_CLOUD_INSTANCE_ID", value = var.grafana_cloud_instance_id },
        { name = "ALLOY_CONFIG_SHA256", value = sha256(aws_ssm_parameter.alloy_config.value) },
      ]
      secrets = [
        { name = "ALLOY_CONFIG_CONTENT", valueFrom = aws_ssm_parameter.alloy_config.arn },
        { name = "GRAFANA_CLOUD_API_KEY", valueFrom = aws_secretsmanager_secret.grafana_cloud.arn },
      ]

      healthCheck = {
        command     = ["CMD", "bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/12345 && printf 'GET /-/ready HTTP/1.0\\r\\n\\r\\n' >&3 && head -1 <&3 | grep -q ' 200 '"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 30
      }

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.app.name
          awslogs-region        = var.region
          awslogs-stream-prefix = "alloy"
        }
      }
    },
  ])
}

resource "aws_ecs_service" "alloy" {
  name            = "${local.name}-alloy"
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.alloy.arn
  desired_count   = 1

  capacity_provider_strategy {
    capacity_provider = "FARGATE"
    weight            = 1
  }

  network_configuration {
    subnets         = module.vpc.private_subnets
    security_groups = [aws_security_group.alloy.id]
  }

  service_registries {
    registry_arn = aws_service_discovery_service.alloy.arn
  }

  lifecycle {
    ignore_changes = [desired_count]
  }

  depends_on = [aws_ecs_cluster_capacity_providers.this]
}
