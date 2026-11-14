package kr.co.deepple.profile.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminMemberStatusInfo(
    @Schema(description = "주요 연락 수단. 값은 회원의 primaryContactType과 같다", example = "KAKAO")
        String primaryContactType,
    boolean hasInterviewAnswer,
    int warningCount,
    boolean isDatingExamSubmitted) {}
