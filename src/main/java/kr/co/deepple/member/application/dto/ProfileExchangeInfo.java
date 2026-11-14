package kr.co.deepple.member.application.dto;

public record ProfileExchangeInfo(
    Long profileExchangeId, Long requesterId, Long responderId, String profileExchangeStatus) {}
