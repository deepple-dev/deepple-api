package kr.co.deepple.member.command.domain.introduction.vo;

import jakarta.persistence.Embeddable;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Embeddable
@EqualsAndHashCode
public class AgeRange {

  private static final Integer MIN_VALUE = 20;
  private static final Integer MAX_VALUE = 46;

  private final Integer minAge;
  private final Integer maxAge;

  protected AgeRange() {
    this.minAge = MIN_VALUE;
    this.maxAge = MAX_VALUE;
  }

  private AgeRange(@NonNull Integer minAge, @NonNull Integer maxAge) {
    validateAgeRange(minAge, maxAge);
    this.minAge = minAge;
    this.maxAge = maxAge;
  }

  public static AgeRange init() {
    return new AgeRange(MIN_VALUE, MAX_VALUE);
  }

  public static AgeRange of(Integer minAge, Integer maxAge) {
    return new AgeRange(minAge, maxAge);
  }

  public static AgeRange ofRange(Integer memberAge, Integer range) {
    if (memberAge == null) {
      return init();
    }
    Integer minAge = Math.max(MIN_VALUE, memberAge - range);
    Integer maxAge = Math.min(MAX_VALUE, memberAge + range);
    return new AgeRange(minAge, maxAge);
  }

  private void validateAgeRange(Integer minAge, Integer maxAge) {
    if (minAge < MIN_VALUE || maxAge > MAX_VALUE || minAge > maxAge) {
      throw new DeeppleException(
          MemberErrorCode.INVALID_AGE_RANGE, "minAge=" + minAge + ", maxAge=" + maxAge);
    }
  }
}
