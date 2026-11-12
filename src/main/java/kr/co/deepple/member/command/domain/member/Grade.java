package kr.co.deepple.member.command.domain.member;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import lombok.Getter;

@Getter
public enum Grade {
  DIAMOND("다이아몬드"),
  GOLD("골드"),
  SILVER("실버");

  private final String description;

  Grade(String description) {
    this.description = description;
  }

  public static Grade from(String grade) {
    try {
      return Grade.valueOf(grade.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new DeeppleException(MemberErrorCode.INVALID_GRADE, "grade=" + grade, e);
    }
  }
}
