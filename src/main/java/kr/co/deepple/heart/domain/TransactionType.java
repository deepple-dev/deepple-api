package kr.co.deepple.heart.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionType {
  MISSION("미션 완료"),
  MESSAGE("메시지 보내기"),
  DATING_EXAM("연애 모의고사 다시보기"),
  PROFILE_EXCHANGE("프로필 교환 신청"),
  INTRODUCTION("소개 받기"),
  PURCHASE("하트 구매"),
  ADMIN_GRANT("관리자 지급"),
  REFUND("하트 환불");

  private final String description;

  public boolean isUsingType() {
    return this == MESSAGE
        || this == DATING_EXAM
        || this == PROFILE_EXCHANGE
        || this == INTRODUCTION;
  }

  public boolean isAddingType() {
    return this == MISSION || this == PURCHASE || this == ADMIN_GRANT;
  }

  public boolean isDeductingType() {
    return isUsingType() || this == REFUND;
  }
}
