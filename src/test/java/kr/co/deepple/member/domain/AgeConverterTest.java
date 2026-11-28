package kr.co.deepple.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AgeConverterTest {

  @Test
  @DisplayName("나이는 연 나이(올해 − 태어난 해)로 계산한다.")
  void toAgeIsYearAge() {
    int currentYear = Calendar.getInstance().get(Calendar.YEAR);

    assertThat(AgeConverter.toAge(currentYear - 25)).isEqualTo(25);
    assertThat(AgeConverter.toYearOfBirth(25)).isEqualTo(currentYear - 25);
  }

  @Test
  @DisplayName("연도를 나이로 변환하고, 다시 나이를 연도로 변환하면 같은 연도가 나와야 한다.")
  void toAgeAndBackThenSameYearOfBirth() {
    // given
    Integer yearOfBirth = 1997;

    // when
    Integer age = AgeConverter.toAge(yearOfBirth);
    Integer convertedYearOfBirth = AgeConverter.toYearOfBirth(age);

    // then
    assertThat(convertedYearOfBirth).isEqualTo(yearOfBirth);
  }

  @Test
  @DisplayName("null이면 null을 돌려준다.")
  void returnsNullWhenNull() {
    assertThat(AgeConverter.toAge(null)).isNull();
    assertThat(AgeConverter.toYearOfBirth(null)).isNull();
  }
}
