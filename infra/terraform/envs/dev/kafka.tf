resource "aws_service_discovery_service" "kafka" {
  name = "kafka"

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
  kafka_host              = "${aws_service_discovery_service.kafka.name}.${aws_service_discovery_private_dns_namespace.this.name}"
  kafka_bootstrap_servers = "${local.kafka_host}:9092"

  kafka_environment = {
    KAFKA_NODE_ID                                          = "1"
    KAFKA_PROCESS_ROLES                                    = "broker,controller"
    KAFKA_CONTROLLER_QUORUM_VOTERS                         = "1@localhost:9093"
    KAFKA_LISTENERS                                        = "PLAINTEXT://:9092,CONTROLLER://:9093"
    KAFKA_ADVERTISED_LISTENERS                             = "PLAINTEXT://${local.kafka_bootstrap_servers}"
    KAFKA_CONTROLLER_LISTENER_NAMES                        = "CONTROLLER"
    KAFKA_LISTENER_SECURITY_PROTOCOL_MAP                   = "PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT"
    KAFKA_INTER_BROKER_LISTENER_NAME                       = "PLAINTEXT"
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR                 = "1"
    KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR         = "1"
    KAFKA_TRANSACTION_STATE_LOG_MIN_ISR                    = "1"
    KAFKA_SHARE_COORDINATOR_STATE_TOPIC_REPLICATION_FACTOR = "1"
    KAFKA_SHARE_COORDINATOR_STATE_TOPIC_MIN_ISR            = "1"
    KAFKA_NUM_PARTITIONS                                   = "6"
    KAFKA_HEAP_OPTS                                        = "-Xms1g -Xmx1g"
  }
}

resource "aws_ecs_task_definition" "kafka" {
  family                   = "${local.name}-kafka"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = 512
  memory                   = 2048
  execution_role_arn       = aws_iam_role.execution.arn

  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "ARM64"
  }

  container_definitions = jsonencode([
    {
      name      = "kafka"
      image     = "${var.account_id}.dkr.ecr.${var.region}.amazonaws.com/docker-hub/apache/kafka:${var.kafka_version}"
      essential = true

      portMappings = [{ containerPort = 9092, protocol = "tcp" }]

      environment = [for name, value in local.kafka_environment : { name = name, value = value }]

      stopTimeout = 30

      healthCheck = {
        command     = ["CMD-SHELL", "bash -c '</dev/tcp/localhost/9092' || exit 1"]
        interval    = 10
        timeout     = 3
        retries     = 3
        startPeriod = 60
      }

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.app.name
          awslogs-region        = var.region
          awslogs-stream-prefix = "kafka"
        }
      }
    },
  ])
}

resource "aws_ecs_service" "kafka" {
  name            = "${local.name}-kafka"
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.kafka.arn
  desired_count   = 1

  deployment_minimum_healthy_percent = 0
  deployment_maximum_percent         = 100

  capacity_provider_strategy {
    capacity_provider = "FARGATE_SPOT"
    weight            = 1
  }

  network_configuration {
    subnets          = module.vpc.public_subnets
    security_groups  = [aws_security_group.kafka.id]
    assign_public_ip = true
  }

  service_registries {
    registry_arn = aws_service_discovery_service.kafka.arn
  }

  lifecycle {
    ignore_changes = [desired_count]
  }

  depends_on = [aws_ecs_cluster_capacity_providers.this]
}
