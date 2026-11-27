package kr.co.deepple.auth.domain;

import kr.co.deepple.common.exception.DeeppleException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WithdrawalReason {
  NEW_RELATIONSHIP("다른 인연이 생겼어요"),
  NO_IDEAL_MATCH("마음에 드는 이상형이 없어요"),
  TOO_EXPENSIVE("과금 유도가 너무 심한 것 같아요"),
  INCONVENIENT("서비스 이용이 너무 불편해요"),
  NO_LIKES_OR_MESSAGES("좋아요나 메시지를 받지 못했어요"),
  NO_REASON("특별한 이유는 없어요");

  private final String description;

  public static WithdrawalReason from(String value) {
    try {
      return WithdrawalReason.valueOf(value);
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new DeeppleException(AuthErrorCode.INVALID_WITHDRAWAL_REASON, "value=" + value, e);
    }
  }
}
