locals {
  kafka_host              = "${aws_service_discovery_service.kafka.name}.${aws_service_discovery_private_dns_namespace.this.name}"
  kafka_bootstrap_servers = "${local.kafka_host}:9092"
  kafka_data_dir          = "/var/lib/kafka/data"
  kafka_az                = module.vpc.azs[0]

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
    KAFKA_AUTO_CREATE_TOPICS_ENABLE                        = "false"
    KAFKA_LOG_DIRS                                         = local.kafka_data_dir
    KAFKA_HEAP_OPTS                                        = "-Xms1g -Xmx1g"
  }
}

resource "aws_security_group" "kafka" {
  name        = "${local.name}-kafka"
  description = "Kafka"
  vpc_id      = module.vpc.vpc_id
}

resource "aws_vpc_security_group_ingress_rule" "kafka_from_app" {
  security_group_id            = aws_security_group.kafka.id
  referenced_security_group_id = aws_security_group.app.id
  ip_protocol                  = "tcp"
  from_port                    = 9092
  to_port                      = 9092
}

resource "aws_vpc_security_group_egress_rule" "kafka_all" {
  security_group_id = aws_security_group.kafka.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

resource "aws_cloudwatch_log_group" "kafka" {
  name              = "/ec2/${local.name}-kafka"
  retention_in_days = 30
}

data "aws_iam_policy_document" "ec2_trust" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "kafka" {
  name               = "${local.name}-kafka"
  description        = "Kafka EC2 instance role for deepple-prod"
  assume_role_policy = data.aws_iam_policy_document.ec2_trust.json
}

resource "aws_iam_role_policy_attachment" "kafka_ssm" {
  role       = aws_iam_role.kafka.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

data "aws_iam_policy_document" "kafka" {
  statement {
    sid       = "EcrAuth"
    actions   = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }

  statement {
    sid = "EcrPull"
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:BatchGetImage",
      "ecr:GetDownloadUrlForLayer",
      "ecr:CreateRepository",
      "ecr:BatchImportUpstreamImage",
    ]
    resources = ["arn:aws:ecr:${var.region}:${var.account_id}:repository/docker-hub/*"]
  }

  statement {
    sid       = "Logs"
    actions   = ["logs:CreateLogStream", "logs:PutLogEvents"]
    resources = ["${aws_cloudwatch_log_group.kafka.arn}:*"]
  }
}

resource "aws_iam_role_policy" "kafka" {
  name   = "kafka"
  role   = aws_iam_role.kafka.id
  policy = data.aws_iam_policy_document.kafka.json
}

resource "aws_iam_instance_profile" "kafka" {
  name = "${local.name}-kafka"
  role = aws_iam_role.kafka.name
}

data "aws_ssm_parameter" "al2023_arm64" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-arm64"
}

resource "aws_ebs_volume" "kafka" {
  availability_zone = local.kafka_az
  type              = "gp3"
  size              = var.kafka.volume_size
  encrypted         = true

  tags = {
    Name     = "${local.name}-kafka-data"
    Snapshot = "${local.name}-kafka"
  }

  lifecycle {
    prevent_destroy = true
  }
}

resource "aws_instance" "kafka" {
  ami                    = data.aws_ssm_parameter.al2023_arm64.insecure_value
  instance_type          = var.kafka.instance_type
  subnet_id              = module.vpc.private_subnets[0]
  vpc_security_group_ids = [aws_security_group.kafka.id]
  iam_instance_profile   = aws_iam_instance_profile.kafka.name

  user_data = templatefile("${path.module}/templates/kafka-user-data.sh.tftpl", {
    volume_id   = aws_ebs_volume.kafka.id
    data_dir    = local.kafka_data_dir
    region      = var.region
    registry    = "${var.account_id}.dkr.ecr.${var.region}.amazonaws.com"
    image       = "${var.account_id}.dkr.ecr.${var.region}.amazonaws.com/docker-hub/apache/kafka:${var.kafka.version}"
    log_group   = aws_cloudwatch_log_group.kafka.name
    environment = join("\n", [for name, value in local.kafka_environment : "${name}=${value}"])
  })
  user_data_replace_on_change = true

  metadata_options {
    http_tokens = "required"
  }

  root_block_device {
    volume_type = "gp3"
    encrypted   = true
  }

  tags = {
    Name = "${local.name}-kafka"
  }

  lifecycle {
    ignore_changes = [ami]
  }

  depends_on = [aws_iam_role_policy.kafka, aws_iam_role_policy_attachment.kafka_ssm]
}

resource "aws_volume_attachment" "kafka" {
  device_name                    = "/dev/sdf"
  volume_id                      = aws_ebs_volume.kafka.id
  instance_id                    = aws_instance.kafka.id
  stop_instance_before_detaching = true
}

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

resource "aws_service_discovery_instance" "kafka" {
  instance_id = aws_instance.kafka.id
  service_id  = aws_service_discovery_service.kafka.id

  attributes = {
    AWS_INSTANCE_IPV4 = aws_instance.kafka.private_ip
  }
}

data "aws_iam_policy_document" "dlm_trust" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["dlm.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "dlm" {
  name               = "${local.name}-dlm"
  description        = "Create and delete EBS snapshots for deepple-prod"
  assume_role_policy = data.aws_iam_policy_document.dlm_trust.json
}

resource "aws_iam_role_policy_attachment" "dlm" {
  role       = aws_iam_role.dlm.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSDataLifecycleManagerServiceRole"
}

resource "aws_dlm_lifecycle_policy" "kafka" {
  description        = "Daily snapshots of deepple-prod Kafka data volume"
  execution_role_arn = aws_iam_role.dlm.arn
  state              = "ENABLED"

  policy_details {
    resource_types = ["VOLUME"]
    target_tags    = { Snapshot = "${local.name}-kafka" }

    schedule {
      name      = "daily"
      copy_tags = true

      create_rule {
        interval      = 24
        interval_unit = "HOURS"
        times         = ["18:00"]
      }

      retain_rule {
        count = var.kafka.snapshot_retention
      }
    }
  }
}
