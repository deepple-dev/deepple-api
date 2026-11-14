package kr.co.deepple.profile.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record StatusInfo(
    Long memberId,
    @Schema(description = "활동 상태. 값은 회원의 activityStatus와 같다", example = "ACTIVE")
        String activityStatus,
    Boolean isVip,
    @Schema(description = "주요 연락 수단. 값은 회원의 primaryContactType과 같다", example = "KAKAO")
        String primaryContactType,
    boolean isDatingExamSubmitted) {}
