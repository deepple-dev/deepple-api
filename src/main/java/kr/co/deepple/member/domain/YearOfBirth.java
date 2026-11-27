package kr.co.deepple.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Calendar;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class YearOfBirth {

  @Column(name = "year_of_birth")
  private Integer value;

  private YearOfBirth(Integer value) {
    this.value = value;
  }

  /** 새로 정하는 태어난 해. 한국식 나이 20~46세만 받는다. */
  public static YearOfBirth from(Integer yearOfBirth) {
    if (yearOfBirth != null) {
      int age = Calendar.getInstance().get(Calendar.YEAR) - yearOfBirth + 1;
      if (age < 20 || age > 46) {
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
