package kr.co.deepple.datingexam.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DatingExamSubmitResponse(
    @Schema(description = "이번 제출로 받은 하트 개수. 받지 않았으면 null", nullable = true)
        Integer rewardedHeartAmount) {}
