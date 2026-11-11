package kr.co.deepple.member.query.member.view;

import com.querydsl.core.annotations.QueryProjection;

public record HeartBalanceView(
    long purchaseHeartBalance,
    long missionHeartBalance,
    long totalHeartBalance
) {
    @QueryProjection
    public HeartBalanceView {
    }
}
