package kr.co.deepple.community.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record SelfIntroductionSummaryView(
    Long id,
    String nickname,
    Integer yearOfBirth,
    String title,
    String content,
    String imageUrl,
    LocalDateTime createdAt,
    @Schema(
            description = "연애가치관 유형. 값은 연애고사 결과의 대표 유형과 같고, 연애고사를 제출하지 않았으면 null이다",
            example = "DECISIVE_INDEPENDENT")
        String personalityType) {}
