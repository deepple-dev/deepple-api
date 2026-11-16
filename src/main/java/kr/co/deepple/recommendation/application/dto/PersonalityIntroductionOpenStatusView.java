package kr.co.deepple.recommendation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record PersonalityIntroductionOpenStatusView(
    @Schema(
            description =
                "연애가치관 유형. 값은 연애고사의 대표 성격 유형과 같다 - DECISIVE_INDEPENDENT: 자기주도형, "
                    + "GROWING_RUNNING_MATE: 동반성장형, DEVOTED_ROMANTIC: 정서교감형, "
                    + "REALISTIC_SHELTER: 현실안정형, STIMULATING_ADVENTURER: 자극모험형, "
                    + "RATIONAL_CENTRIST: 이성중심형",
            example = "DECISIVE_INDEPENDENT")
        String type,
    LocalDateTime openedAt) {}
