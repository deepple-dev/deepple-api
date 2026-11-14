package kr.co.deepple.recommendation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record PersonalityIntroductionOpenStatusView(
    @Schema(
            description =
                "연애가치관 유형. 값은 연애고사의 대표 성격 유형과 같다 - DECISIVE_INDEPENDENT: 단호한 독립주의자, "
                    + "GROWING_RUNNING_MATE: 성장하는 러닝메이트, DEVOTED_ROMANTIC: 헌신적인 로맨티스트, "
                    + "REALISTIC_SHELTER: 현실적인 안식처, STIMULATING_ADVENTURER: 자극모험형, "
                    + "RATIONAL_REALIST: 이성중심형",
            example = "DECISIVE_INDEPENDENT")
        String type,
    LocalDateTime openedAt) {}
