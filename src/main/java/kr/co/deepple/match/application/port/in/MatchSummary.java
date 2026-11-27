package kr.co.deepple.match.application.port.in;

import java.time.LocalDateTime;

public record MatchSummary(
    long matchId,
    long requesterId,
    long responderId,
    String requestMessage,
    String responseMessage,
    String status,
    String requesterContactType,
    String responderContactType,
    LocalDateTime readByResponderAt) {}
