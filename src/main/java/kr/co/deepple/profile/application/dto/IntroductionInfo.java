package kr.co.deepple.profile.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record IntroductionInfo(
    @Schema(description = "소개 종류. 값은 이성 소개의 type과 같다", example = "DIAMOND_GRADE")
        String introductionType) {}
