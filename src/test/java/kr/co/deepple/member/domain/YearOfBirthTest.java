package kr.co.deepple.member.domain;

import java.util.Calendar;
import kr.co.deepple.common.exception.DeeppleException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class YearOfBirthTest {

  @DisplayName("연 나이가 19살 미만이거나 45살 초과인 태어난 해인 경우, 예외 발생")
  @ParameterizedTest
  @ValueSource(ints = {18, 46})
  void throwExceptionWhenAgeIsOutOfRange(int age) {
    // Given
    int yearOfBirth = AgeConverter.toYearOfBirth(age);

    // When & Then
    Assertions.assertThatThrownBy(() -> YearOfBirth.from(yearOfBirth))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.INVALID_YEAR_OF_BIRTH);
  }

  @DisplayName("이미 저장된 태어난 해를 그대로 두면 나이 범위를 넘었어도 정상 동작")
  @ParameterizedTest
  @ValueSource(ints = {18, 46})
  void keepStoredYearOfBirthEvenWhenAgeIsOutOfRange(int age) {
    // Given
    int yearOfBirth = AgeConverter.toYearOfBirth(age);

    // When
    YearOfBirth result = YearOfBirth.unchanged(yearOfBirth);

    // Then
    Assertions.assertThat(result.getValue()).isEqualTo(yearOfBirth);
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

  @DisplayName("연 나이가 19살 이상 45살 이하인 태어난 해인 경우, 정상 동작")
  @ParameterizedTest
  @ValueSource(ints = {19, 45})
  void createYearOfBirthWhenAgeIsInRange(int age) {
    // Given
    int yearOfBirth = AgeConverter.toYearOfBirth(age);

    // When
    YearOfBirth result = YearOfBirth.from(yearOfBirth);

    // Then
    Assertions.assertThat(result.getValue()).isEqualTo(yearOfBirth);
    Assertions.assertThat(result.getAge()).isEqualTo(age);
  }

  @DisplayName("가입할 수 있는 태어난 해는 올해−45년부터 올해−19년까지다")
  @Test
  void acceptsSameBirthYearsAsBefore() {
    // Given
    int currentYear = Calendar.getInstance().get(Calendar.YEAR);

    // When & Then
    Assertions.assertThat(YearOfBirth.from(currentYear - 19).getValue())
        .isEqualTo(currentYear - 19);
    Assertions.assertThat(YearOfBirth.from(currentYear - 45).getValue())
        .isEqualTo(currentYear - 45);
    Assertions.assertThatThrownBy(() -> YearOfBirth.from(currentYear - 18))
        .isInstanceOf(DeeppleException.class);
    Assertions.assertThatThrownBy(() -> YearOfBirth.from(currentYear - 46))
        .isInstanceOf(DeeppleException.class);
  }
}
