package kr.co.deepple.datingexam.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class AnswerPersonalityTypeTest {

  @ParameterizedTest
  @EnumSource(AnswerPersonalityType.class)
  @DisplayName("모든 유형은 자신이 아닌 잘 맞는 유형 2개를 가진다.")
  void everyTypeHasTwoOtherCompatibleTypes(AnswerPersonalityType type) {
    assertThat(type.compatibleTypes()).hasSize(2).doesNotContain(type).doesNotHaveDuplicates();
  }

  @ParameterizedTest
  @CsvSource({
    "DECISIVE_INDEPENDENT, GROWING_RUNNING_MATE, DEVOTED_ROMANTIC",
    "GROWING_RUNNING_MATE, DEVOTED_ROMANTIC, REALISTIC_SHELTER",
    "DEVOTED_ROMANTIC, REALISTIC_SHELTER, STIMULATING_ADVENTURER",
    "REALISTIC_SHELTER, STIMULATING_ADVENTURER, RATIONAL_CENTRIST",
    "STIMULATING_ADVENTURER, RATIONAL_CENTRIST, DECISIVE_INDEPENDENT",
    "RATIONAL_CENTRIST, DECISIVE_INDEPENDENT, GROWING_RUNNING_MATE"
  })
  @DisplayName("잘 맞는 유형은 앱·리디자인 결과 화면 기준(유형 순서상 다음 두 유형)이다.")
  void compatibleTypesFollowAppRule(
      AnswerPersonalityType type, AnswerPersonalityType first, AnswerPersonalityType second) {
    assertThat(type.compatibleTypes()).containsExactly(first, second);
  }
}
