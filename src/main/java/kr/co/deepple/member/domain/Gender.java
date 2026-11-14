package kr.co.deepple.member.domain;

import kr.co.deepple.common.exception.DeeppleException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
  MALE("남성"),
  FEMALE("여성");

  private final String description;

  public static Gender from(String value) {
    if (value == null) {
      return null;
    }

    try {
      return Gender.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new DeeppleException(MemberErrorCode.INVALID_PROFILE_OPTION, "Gender=" + value, e);
    }
  }

  public Gender getOpposite() {
    return this == MALE ? FEMALE : MALE;
  }
}
