package kr.co.deepple.conversation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ConversationContactView(
    @Schema(description = "상대가 공개한 연락 수단", example = "PHONE_NUMBER") String contactType,
    @Schema(description = "전화번호 또는 카카오톡 ID") String contact) {}
