package kr.co.deepple.conversation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record ConversationMatchView(
    long matchId,
    @Schema(description = "매칭 상태. 값은 매칭의 status와 같다", example = "WAITING") String status,
    @Schema(description = "내가 메시지를 보낸 쪽인지") boolean isRequester,
    @Schema(description = "상대가 내 메시지를 읽은 시각. 내가 보낸 쪽일 때만, 읽지 않았으면 null") LocalDateTime readAt) {}
