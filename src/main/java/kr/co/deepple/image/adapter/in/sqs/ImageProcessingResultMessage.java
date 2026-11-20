package kr.co.deepple.image.adapter.in.sqs;

public record ImageProcessingResultMessage(String sourceKey, String status, String failureReason) {}
