package kr.co.deepple.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class YearOfBirth {

  private static final int MIN_AGE = 19;
  private static final int MAX_AGE = 45;

  @Column(name = "year_of_birth")
  private Integer value;

  private YearOfBirth(Integer value) {
    this.value = value;
  }

  /** 새로 정하는 태어난 해. 연 나이(올해 − 태어난 해) 19~45세만 받는다. */
  public static YearOfBirth from(Integer yearOfBirth) {
    if (yearOfBirth != null) {
      int age = AgeConverter.toAge(yearOfBirth);
      if (age < MIN_AGE || age > MAX_AGE) {
        throw new DeeppleException(
            MemberErrorCode.INVALID_YEAR_OF_BIRTH, "yearOfBirth=" + yearOfBirth);
      }
    }
    return new YearOfBirth(yearOfBirth);
  }

  /** 이미 저장된 태어난 해를 그대로 두는 경우. 가입 뒤 해가 바뀌어 범위를 넘겼어도 다른 항목을 수정할 수 있게 범위를 다시 검사하지 않는다. */
  public static YearOfBirth unchanged(Integer yearOfBirth) {
    return new YearOfBirth(yearOfBirth);
  }

  public Integer getAge() {
    if (value == null) {
      return null;
    }
    return AgeConverter.toAge(value);
  }
}
