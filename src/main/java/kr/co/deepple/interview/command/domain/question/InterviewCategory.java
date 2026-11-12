package kr.co.deepple.interview.command.domain.question;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.interview.command.domain.InterviewErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum InterviewCategory {
  PERSONAL("나"),
  SOCIAL("관계"),
  ROMANTIC("연애");

  private final String description;

  public static InterviewCategory from(String value) {
    if (value == null) {
      return null;
    }

    try {
      return InterviewCategory.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new DeeppleException(
          InterviewErrorCode.INVALID_INTERVIEW_CATEGORY, "value=" + value, e);
    }
  }
}
