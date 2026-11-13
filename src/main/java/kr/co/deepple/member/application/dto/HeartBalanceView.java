package kr.co.deepple.member.application.dto;

import com.querydsl.core.annotations.QueryProjection;

public record HeartBalanceView(
    long purchaseHeartBalance, long missionHeartBalance, long totalHeartBalance) {
  @QueryProjection
  public HeartBalanceView {}
}
