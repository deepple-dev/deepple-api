package kr.co.deepple.member.domain;

import java.util.Calendar;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** 나이는 연 나이(올해 − 태어난 해)로 계산한다. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AgeConverter {

  public static Integer toAge(Integer yearOfBirth) {
    return yearOfBirth == null ? null : Calendar.getInstance().get(Calendar.YEAR) - yearOfBirth;
  }

  public static Integer toYearOfBirth(Integer age) {
    return age == null ? null : Calendar.getInstance().get(Calendar.YEAR) - age;
  }
}
