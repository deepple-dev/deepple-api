package kr.co.deepple.recommendation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.co.deepple.common.exception.DeeppleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AgeRangeTest {

  @Test
  @DisplayName("처음 값은 가입할 수 있는 연 나이 전체(19~45)다.")
  void initIsWholeRange() {
    AgeRange ageRange = AgeRange.init();

    assertThat(ageRange.getMinAge()).isEqualTo(19);
    assertThat(ageRange.getMaxAge()).isEqualTo(45);
  }

  @ParameterizedTest
  @CsvSource({"18, 30", "20, 46", "30, 25"})
  @DisplayName("19~45를 벗어나거나 최소가 최대보다 크면 예외 발생")
  void throwsWhenOutOfRange(int minAge, int maxAge) {
    assertThatThrownBy(() -> AgeRange.of(minAge, maxAge))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", RecommendationErrorCode.INVALID_AGE_RANGE);
  }

  @Test
  @DisplayName("회원 나이 앞뒤 범위는 19~45 안으로 자른다.")
  void ofRangeClampsToAllowedRange() {
    AgeRange young = AgeRange.ofRange(20, 3);
    AgeRange old = AgeRange.ofRange(44, 3);

    assertThat(young.getMinAge()).isEqualTo(19);
    assertThat(young.getMaxAge()).isEqualTo(23);
    assertThat(old.getMinAge()).isEqualTo(41);
    assertThat(old.getMaxAge()).isEqualTo(45);
  }

  @Test
  @DisplayName("가입 뒤 해가 지나 45세를 넘긴 회원도 범위가 뒤집히지 않는다.")
  void ofRangeDoesNotInvertForOlderMember() {
    AgeRange ageRange = AgeRange.ofRange(50, 3);

    assertThat(ageRange.getMinAge()).isEqualTo(45);
    assertThat(ageRange.getMaxAge()).isEqualTo(45);
  }

  @Test
  @DisplayName("회원이 고른 범위에서 45를 넘는 값(옛 앱의 46)은 45로 자른다.")
  void ofRequestedClampsOldAppMaximum() {
    AgeRange ageRange = AgeRange.ofRequested(20, 46);
    AgeRange bothAtEnd = AgeRange.ofRequested(46, 46);

    assertThat(ageRange.getMinAge()).isEqualTo(20);
    assertThat(ageRange.getMaxAge()).isEqualTo(45);
    assertThat(bothAtEnd.getMinAge()).isEqualTo(45);
    assertThat(bothAtEnd.getMaxAge()).isEqualTo(45);
  }
}
