package kr.co.deepple.member.query.member;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AgeConverterTest {

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
}
