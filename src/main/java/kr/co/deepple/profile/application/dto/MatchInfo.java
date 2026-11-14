package kr.co.deepple.profile.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

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
        String responderContactType) {}
