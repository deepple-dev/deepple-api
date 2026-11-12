package kr.co.deepple.heart.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record HeartBalanceView(
    @Schema(description = "구매 하트 잔액", example = "10")
    long purchaseHeartBalance,
    @Schema(description = "미션 하트 잔액", example = "5")
    long missionHeartBalance,
    @Schema(description = "전체 하트 잔액", example = "15")
    long totalHeartBalance
) {
    public static HeartBalanceView empty() {
        return new HeartBalanceView(0L, 0L, 0L);
    }
}
