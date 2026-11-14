package kr.co.deepple.profile.application.dto;

public record ProfileExchangeInfo(
    Long profileExchangeId, Long requesterId, Long responderId, String profileExchangeStatus) {}
