package kr.co.deepple.conversation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record ConversationView(
    @Schema(description = "상대 회원 ID. 메시지 상세는 이 ID로 조회한다") long memberId,
    String nickname,
    @Schema(description = "상대 대표 사진") String profileImageUrl,
    @Schema(description = "가장 최근 일") ConversationEventView lastEvent,
    @Schema(
            description =
                "진행 중인(없으면 가장 최근) 매칭에서 상대가 쓴 메시지(받은 메시지 또는 답장). 없으면 null이고 앱은 상태 문구를 보여 준다")
        String counterpartMessage,
    @Schema(description = "내가 답해야 하는지(받은 메시지·교환 요청이 대기 중이고 상대가 활동 중). 빨간 점 표시")
        boolean needsMyAction,
    @Schema(description = "상대가 활동 중인지. 탈퇴·정지·휴면이면 false") boolean isCounterpartActive,
    LocalDateTime lastActivityAt) {}
