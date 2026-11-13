package kr.co.deepple.recommendation.domain;

import jakarta.persistence.Embeddable;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class AgeRange {

  private static final int MIN_VALUE = 20;
  private static final int MAX_VALUE = 46;

  private final int minAge;
  private final int maxAge;

  private AgeRange(int minAge, int maxAge) {
    if (minAge < MIN_VALUE || maxAge > MAX_VALUE || minAge > maxAge) {
      throw new DeeppleException(
          MemberErrorCode.INVALID_AGE_RANGE, "minAge=" + minAge + ", maxAge=" + maxAge);
    }
    this.minAge = minAge;
    this.maxAge = maxAge;
  }

  public static AgeRange init() {
    return new AgeRange(MIN_VALUE, MAX_VALUE);
  }

  public static AgeRange of(int minAge, int maxAge) {
    return new AgeRange(minAge, maxAge);
  }

  public static AgeRange ofRange(Integer memberAge, int range) {
    if (memberAge == null) {
      return init();
    }
    int minAge = Math.max(MIN_VALUE, memberAge - range);
    int maxAge = Math.min(MAX_VALUE, memberAge + range);
    return new AgeRange(minAge, maxAge);
  }
}
