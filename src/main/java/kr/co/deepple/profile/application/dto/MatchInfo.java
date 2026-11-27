package kr.co.deepple.profile.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record MatchInfo(
    Long matchId,
    Long requesterId,
    Long responderId,
    String requestMessage,
    String responseMessage,
    @Schema(description = "매칭 상태. 값은 매칭의 status와 같다", example = "MATCHED") String matchStatus,
    @Schema(description = "요청자가 공개한 연락 수단. 값은 매칭의 contactType과 같다", example = "KAKAO")
        String requesterContactType,
    @Schema(description = "응답자가 공개한 연락 수단. 값은 매칭의 contactType과 같다", example = "PHONE_NUMBER")
        String responderContactType,
    @Schema(description = "상대(응답자)가 내 메시지를 읽은 시각. 내가 요청자일 때만 내려 주고, 읽지 않았거나 내가 응답자면 null이다")
        LocalDateTime readAt) {}
