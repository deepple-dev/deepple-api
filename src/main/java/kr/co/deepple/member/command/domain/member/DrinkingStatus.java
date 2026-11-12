package kr.co.deepple.member.command.domain.member;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DrinkingStatus {
  NONE("전혀 마시지 않음"),
  QUIT("금주"),
  SOCIAL("사회적 음주"),
  OCCASIONAL("가끔 마심"),
  FREQUENT("술자리를 즐김");

  private final String description;

  public static DrinkingStatus from(String value) {
    if (value == null || value.isEmpty()) {
      return null;
    }

    try {
      return DrinkingStatus.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new DeeppleException(
          MemberErrorCode.INVALID_PROFILE_OPTION, "DrinkingStatus=" + value, e);
    }
  }
}
