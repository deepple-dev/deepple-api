package kr.co.deepple.recommendation.domain;

import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class AgeRange {

  /** 연 나이(올해 − 태어난 해). 가입할 수 있는 나이와 같다. DB에는 한국식 나이로 저장한다({@link KoreanAgeColumnConverter}). */
  private static final int MIN_VALUE = 19;

  private static final int MAX_VALUE = 45;

  @Convert(converter = KoreanAgeColumnConverter.class)
  private final int minAge;

  @Convert(converter = KoreanAgeColumnConverter.class)
  private final int maxAge;

  private AgeRange(int minAge, int maxAge) {
    if (minAge < MIN_VALUE || maxAge > MAX_VALUE || minAge > maxAge) {
      throw new DeeppleException(
          RecommendationErrorCode.INVALID_AGE_RANGE, "minAge=" + minAge + ", maxAge=" + maxAge);
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

  /**
   * 회원이 고른 범위. 45를 넘는 값은 45로 자른다. 한국식 20~46 슬라이더를 쓰는 옛 앱이 끝값 46을 보내도 저장되게 하려는 것으로, 모든 앱이 19~45를 쓰면
   * {@link #of}로 바꾼다.
   */
  public static AgeRange ofRequested(int minAge, int maxAge) {
    return new AgeRange(Math.min(minAge, MAX_VALUE), Math.min(maxAge, MAX_VALUE));
  }

  public static AgeRange ofRange(Integer memberAge, int range) {
    if (memberAge == null) {
      return init();
    }
    return new AgeRange(clamp(memberAge - range), clamp(memberAge + range));
  }

  /** 범위를 19~45 안으로 자른다. 가입 뒤 해가 지나 45세를 넘긴 회원도 범위가 뒤집히지 않는다. */
  private static int clamp(int age) {
    return Math.clamp(age, MIN_VALUE, MAX_VALUE);
  }
}
