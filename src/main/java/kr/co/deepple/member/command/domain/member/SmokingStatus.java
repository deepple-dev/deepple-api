package kr.co.deepple.member.command.domain.member;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SmokingStatus {
  NONE("비흡연"),
  QUIT("금연"),
  OCCASIONAL("가끔 피움"),
  DAILY("매일 피움"),
  VAPE("전자담배");

  private final String description;

  public static SmokingStatus from(String value) {
    if (value == null || value.isEmpty()) {
      return null;
    }

    try {
      return SmokingStatus.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new DeeppleException(
          MemberErrorCode.INVALID_PROFILE_OPTION, "SmokingStatus=" + value, e);
    }
  }
}
