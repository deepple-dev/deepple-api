package kr.co.deepple.match.application.port.in;

public record MatchSummary(
    long matchId,
    long requesterId,
    long responderId,
    String requestMessage,
    String responseMessage,
    String status,
    String requesterContactType,
    String responderContactType) {}
