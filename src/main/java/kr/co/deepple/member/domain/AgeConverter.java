package kr.co.deepple.member.domain;

import java.util.Calendar;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AgeConverter {

  public static Integer toAge(Integer yearOfBirth) {
    return yearOfBirth == null ? null : Calendar.getInstance().get(Calendar.YEAR) - yearOfBirth + 1;
  }

  public static Integer toYearOfBirth(Integer age) {
    return age == null ? null : Calendar.getInstance().get(Calendar.YEAR) - age + 1;
  }
}
