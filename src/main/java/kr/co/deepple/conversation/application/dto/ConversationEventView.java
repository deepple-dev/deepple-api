package kr.co.deepple.conversation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.co.deepple.conversation.domain.ConversationEventType;

public record ConversationEventView(
    @Schema(
            implementation = ConversationEventType.class,
            description =
                "PROFILE_EXCHANGE_REQUESTED: 프로필 교환 요청, PROFILE_EXCHANGE_APPROVED: 교환 수락, "
                    + "PROFILE_EXCHANGE_REJECTED: 교환 거절, MESSAGE_SENT: 메시지 보냄, "
                    + "MESSAGE_ACCEPTED: 답장(연락처 공개), MESSAGE_REJECTED: 메시지 거절, MESSAGE_EXPIRED: 답이 없어 만료")
        String type,
    @Schema(description = "내가 한 일인지. 만료는 false") boolean isMine,
    LocalDateTime occurredAt,
    @Schema(description = "메시지 내용. MESSAGE_SENT는 보낸 메시지, MESSAGE_ACCEPTED는 답장. 그 밖에는 null")
        String message,
    @Schema(description = "메시지 이벤트의 매칭 ID. 교환 이벤트면 null") Long matchId,
    @Schema(description = "교환 이벤트의 프로필 교환 ID. 메시지 이벤트면 null") Long profileExchangeId) {}
