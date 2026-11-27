package kr.co.deepple.conversation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ConversationProfileExchangeView(
    long profileExchangeId,
    @Schema(description = "프로필 교환 상태. 값은 프로필 교환의 status와 같다", example = "WAITING") String status,
    @Schema(description = "SENT: 내가 신청, RECEIVED: 상대가 신청", example = "RECEIVED")
        String direction) {}
