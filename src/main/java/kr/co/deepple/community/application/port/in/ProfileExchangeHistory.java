package kr.co.deepple.community.application.port.in;

import java.time.LocalDateTime;

public record ProfileExchangeHistory(
    long profileExchangeId,
    long requesterId,
    long responderId,
    String status,
    LocalDateTime requestedAt,
    LocalDateTime respondedAt) {}
