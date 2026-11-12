package kr.co.deepple.member.query.member.view;

import com.querydsl.core.annotations.QueryProjection;

public record ProfileAccessView(
    boolean isIntroduced,
    Long matchRequesterId,
    Long matchResponderId,
    Long profileExchangeRequesterId,
    Long profileExchangeResponderId,
    String profileExchangeStatus,
    boolean likeReceived,
    boolean isBlocked,
    String activityStatus) {
  @QueryProjection
  public ProfileAccessView {}
}
