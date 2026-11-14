package kr.co.deepple.recommendation.application.dto;

import jakarta.validation.constraints.NotNull;

public record MemberIntroductionCreateRequest(
    @NotNull(message = "introducedMemberId는 필수입니다.") Long introducedMemberId) {}
