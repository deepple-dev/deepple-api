package kr.co.deepple.member.command.domain.member.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Calendar;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.query.member.AgeConverter;
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
    if (value != null) {
      int age = Calendar.getInstance().get(Calendar.YEAR) - value + 1;
      if (age < 20 || age > 46) {
        throw new DeeppleException(MemberErrorCode.INVALID_YEAR_OF_BIRTH, "yearOfBirth=" + value);
      }
    }
    this.value = value;
  }

  public static YearOfBirth from(Integer yearOfBirth) {
    return new YearOfBirth(yearOfBirth);
  }

  public Integer getAge() {
    if (value == null) {
      return null;
    }
    return AgeConverter.toAge(value);
  }
}
