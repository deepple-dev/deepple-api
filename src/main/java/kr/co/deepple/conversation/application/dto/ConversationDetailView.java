package kr.co.deepple.conversation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ConversationDetailView(
    long memberId,
    String nickname,
    @Schema(description = "상대 대표 사진") String profileImageUrl,
    Integer age,
    @Schema(description = "성별. 값은 회원 프로필의 gender와 같다", example = "FEMALE") String gender,
    @Schema(description = "시간 순(오래된 것부터) 타임라인") List<ConversationEventView> events,
    @Schema(description = "지금 진행 중인 매칭(만료·거절 확인된 것 제외). 없으면 null") ConversationMatchView match,
    @Schema(description = "프로필 교환. 없으면 null") ConversationProfileExchangeView profileExchange,
    @Schema(description = "매칭이 성사됐고 상대가 활동 중일 때 상대 연락처. 아니면 null") ConversationContactView contact,
    @Schema(description = "내가 답해야 하는지") boolean needsMyAction,
    @Schema(description = "상대가 활동 중인지. 탈퇴·정지·휴면이면 false") boolean isCounterpartActive,
    @Schema(
            description =
                "상대 프로필 조회(GET /member/{memberId})가 허용되는지. 소개받음·메시지 받음·상대가 좋아요 보냄·프로필 교환 수락됨·교환 요청 받음 중 하나이고 상대가 활동 중일 때 true")
        boolean canViewProfile) {}
