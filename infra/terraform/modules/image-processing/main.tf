locals {
  function_name = "${var.name}-image-processor"
}

resource "aws_sqs_queue" "uploads_dlq" {
  name                      = "${var.name}-image-uploads-dlq"
  message_retention_seconds = 1209600
}

resource "aws_sqs_queue" "uploads" {
  name                       = "${var.name}-image-uploads"
  visibility_timeout_seconds = var.function_timeout_seconds * 6
  message_retention_seconds  = 345600

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.uploads_dlq.arn
    maxReceiveCount     = var.max_receive_count
  })
}

resource "aws_sqs_queue_redrive_allow_policy" "uploads_dlq" {
  queue_url = aws_sqs_queue.uploads_dlq.id

  redrive_allow_policy = jsonencode({
    redrivePermission = "byQueue"
    sourceQueueArns   = [aws_sqs_queue.uploads.arn]
  })
}

data "aws_iam_policy_document" "uploads" {
  statement {
    sid       = "S3Notification"
    actions   = ["sqs:SendMessage"]
    resources = [aws_sqs_queue.uploads.arn]

    principals {
      type        = "Service"
      identifiers = ["s3.amazonaws.com"]
    }

    condition {
      test     = "ArnEquals"
      variable = "aws:SourceArn"
      values   = [var.bucket_arn]
    }

    condition {
      test     = "StringEquals"
      variable = "aws:SourceAccount"
      values   = [var.account_id]
    }
  }
}

resource "aws_sqs_queue_policy" "uploads" {
  queue_url = aws_sqs_queue.uploads.id
  policy    = data.aws_iam_policy_document.uploads.json
}

resource "aws_s3_bucket_notification" "uploads" {
  bucket = var.bucket_id

  queue {
    queue_arn     = aws_sqs_queue.uploads.arn
    events        = ["s3:ObjectCreated:*"]
    filter_prefix = var.source_prefix
  }

  depends_on = [aws_sqs_queue_policy.uploads]
}

resource "aws_sqs_queue" "results_dlq" {
  name                      = "${var.name}-image-results-dlq"
  message_retention_seconds = 1209600
}

resource "aws_sqs_queue" "results" {
  name                       = "${var.name}-image-results"
  visibility_timeout_seconds = 30
  message_retention_seconds  = 345600

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.results_dlq.arn
    maxReceiveCount     = var.max_receive_count
  })
}

resource "aws_sqs_queue_redrive_allow_policy" "results_dlq" {
  queue_url = aws_sqs_queue.results_dlq.id

  redrive_allow_policy = jsonencode({
    redrivePermission = "byQueue"
    sourceQueueArns   = [aws_sqs_queue.results.arn]
  })
}

data "archive_file" "placeholder" {
  type        = "zip"
  output_path = "${path.root}/build/image_processor_placeholder.zip"

  source {
    filename = "index.mjs"
    content  = "export const handler = async () => { throw new Error('코드 배포 전'); };\n"
  }
}

resource "aws_cloudwatch_log_group" "function" {
  name              = "/aws/lambda/${local.function_name}"
  retention_in_days = var.log_retention_days
}

data "aws_iam_policy_document" "function_trust" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }

    condition {
      test     = "StringEquals"
      variable = "aws:SourceAccount"
      values   = [var.account_id]
    }
  }
}

resource "aws_iam_role" "function" {
  name               = local.function_name
  description        = "Convert uploaded images for ${var.name}"
  assume_role_policy = data.aws_iam_policy_document.function_trust.json
}

data "aws_iam_policy_document" "function" {
  statement {
    sid       = "WriteLogs"
    actions   = ["logs:CreateLogStream", "logs:PutLogEvents"]
    resources = ["${aws_cloudwatch_log_group.function.arn}:log-stream:*"]
  }

  statement {
    sid       = "ReadSource"
    actions   = ["s3:GetObject"]
    resources = ["${var.bucket_arn}/${var.source_prefix}*"]
  }

  statement {
    sid       = "ListSource"
    actions   = ["s3:ListBucket"]
    resources = [var.bucket_arn]
  }

  statement {
    sid       = "WriteOutput"
    actions   = ["s3:PutObject"]
    resources = ["${var.bucket_arn}/${var.output_prefix}*"]
  }

  statement {
    sid       = "ConsumeUploads"
    actions   = ["sqs:ReceiveMessage", "sqs:DeleteMessage", "sqs:GetQueueAttributes"]
    resources = [aws_sqs_queue.uploads.arn]
  }

  statement {
    sid       = "SendResults"
    actions   = ["sqs:SendMessage"]
    resources = [aws_sqs_queue.results.arn]
  }
}

resource "aws_iam_role_policy" "function" {
  name   = "image-processor"
  role   = aws_iam_role.function.id
  policy = data.aws_iam_policy_document.function.json
}

resource "aws_lambda_function" "this" {
  function_name = local.function_name
  role          = aws_iam_role.function.arn
  runtime       = "nodejs24.x"
  architectures = ["arm64"]
  handler       = "index.handler"
  filename      = data.archive_file.placeholder.output_path
  timeout       = var.function_timeout_seconds
  memory_size   = var.function_memory_mb

  environment {
    variables = {
      RESULT_QUEUE_URL = aws_sqs_queue.results.url
      SOURCE_PREFIX    = var.source_prefix
      OUTPUT_PREFIX    = var.output_prefix
    }
  }

  logging_config {
    log_format = "JSON"
    log_group  = aws_cloudwatch_log_group.function.name
  }

  lifecycle {
    ignore_changes = [filename, source_code_hash]
  }

  depends_on = [aws_iam_role_policy.function]
}

resource "aws_lambda_event_source_mapping" "uploads" {
  event_source_arn        = aws_sqs_queue.uploads.arn
  function_name           = aws_lambda_function.this.arn
  batch_size              = 5
  function_response_types = ["ReportBatchItemFailures"]

  scaling_config {
    maximum_concurrency = var.maximum_concurrency
  }
}

resource "aws_cloudwatch_metric_alarm" "dlq" {
  for_each = {
    uploads = aws_sqs_queue.uploads_dlq.name
    results = aws_sqs_queue.results_dlq.name
  }

  alarm_name = "${each.value}-not-empty"
  alarm_description = jsonencode({
    title = "Image DLQ not empty (${each.value})"
    what  = "이미지 처리 DLQ 메시지 {value} ({condition})"
    why   = each.key == "uploads" ? ["변환 Lambda 오류", "S3·SQS 일시 장애"] : ["백엔드 결과 처리 오류", "DB 장애"]
    how   = each.key == "uploads" ? ["Lambda 로그(/aws/lambda/${local.function_name}) 확인", "원인을 고친 뒤 DLQ redrive"] : ["앱 로그에서 결과 큐 소비 오류 확인", "원인을 고친 뒤 DLQ redrive"]
    unit  = "count"
  })
  namespace           = "AWS/SQS"
  metric_name         = "ApproximateNumberOfMessagesVisible"
  dimensions          = { QueueName = each.value }
  statistic           = "Maximum"
  period              = 300
  evaluation_periods  = 1
  threshold           = 0
  comparison_operator = "GreaterThanThreshold"
  treat_missing_data  = "notBreaching"
  alarm_actions       = [var.alarm_topic_arn]
}
