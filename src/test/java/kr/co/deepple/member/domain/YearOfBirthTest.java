package kr.co.deepple.member.domain;

import kr.co.deepple.common.exception.DeeppleException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class YearOfBirthTest {

  @DisplayName("나이가 20살 미만이거나 46살 초과인 태어난 해인 경우, 예외 발생")
  @ParameterizedTest
  @ValueSource(ints = {19, 47})
  void throwExceptionWhenAgeIsOutOfRange(int age) {
    // Given
    int yearOfBirth = AgeConverter.toYearOfBirth(age);

    // When & Then
    Assertions.assertThatThrownBy(() -> YearOfBirth.from(yearOfBirth))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.INVALID_YEAR_OF_BIRTH);
  }

  @DisplayName("태어난 해가 null 인 경우, 정상 동작")
  @Test
  void createYearOfBirthWithNull() {
    // When
    YearOfBirth yearOfBirth = YearOfBirth.from(null);

    // Then
    Assertions.assertThat(yearOfBirth).isNotNull();
    Assertions.assertThat(yearOfBirth.getValue()).isNull();
  }

  @DisplayName("나이가 20살 이상 46살 이하인 태어난 해인 경우, 정상 동작")
  @ParameterizedTest
  @ValueSource(ints = {20, 46})
  void createYearOfBirthWhenAgeIsInRange(int age) {
    // Given
    int yearOfBirth = AgeConverter.toYearOfBirth(age);

    // When
    YearOfBirth result = YearOfBirth.from(yearOfBirth);

    // Then
    Assertions.assertThat(result.getValue()).isEqualTo(yearOfBirth);
    Assertions.assertThat(result.getAge()).isEqualTo(age);
  }
}
