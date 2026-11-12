package kr.co.deepple.member.presentation.member.dto;

import java.time.LocalDateTime;

public record TemporarySuspensionLoginResponse(String message, LocalDateTime suspensionExpireAt) {}
