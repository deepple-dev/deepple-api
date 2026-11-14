package kr.co.deepple.community.application.port.in;

public record ProfileExchangeSummary(
    long profileExchangeId, long requesterId, long responderId, String status) {}
