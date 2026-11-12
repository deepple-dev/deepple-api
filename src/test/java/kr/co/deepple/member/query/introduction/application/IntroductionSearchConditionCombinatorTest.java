package kr.co.deepple.member.query.introduction.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import kr.co.deepple.member.command.domain.member.City;
import kr.co.deepple.member.command.domain.member.DrinkingStatus;
import kr.co.deepple.member.command.domain.member.Gender;
import kr.co.deepple.member.command.domain.member.Grade;
import kr.co.deepple.member.command.domain.member.Hobby;
import kr.co.deepple.member.command.domain.member.Religion;
import kr.co.deepple.member.command.domain.member.SmokingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IntroductionSearchConditionCombinatorTest {

  @Test
  @DisplayName("파라미터 위치에 맞게 생성하는지 테스트")
  void generateCombinations() {
    // Given
    IntroductionSearchCondition base = createCondition();

    // When
    List<IntroductionSearchCondition> combinations =
        IntroductionSearchConditionCombinator.generateCombinations(base, 0);

    // Then
    assertThat(combinations).hasSize(1);
    IntroductionSearchCondition condition = combinations.get(0);
    assertCondition(condition, base);
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 4, 5})
  @DisplayName("상관없음 옵션 수와 비 선택 가능 옵션 수에 따른 조합 수 테스트")
  void generateCombinationsWithNonSelectableOptions(int nullableCount) {
    // Given
    // nullableCount에 따라 하나씩 상관없음 옵션으로 설정
    IntroductionSearchCondition base =
        IntroductionSearchCondition.of(
            Set.of(1L, 2L),
            25,
            30,
            nullableCount > 0 ? Set.of() : Set.of(Hobby.ANIMATION.name()),
            nullableCount > 1 ? Set.of() : Set.of(City.BUSAN.name()),
            nullableCount > 2 ? null : Religion.BUDDHIST.name(),
            nullableCount > 3 ? null : SmokingStatus.DAILY.name(),
            nullableCount > 4 ? null : DrinkingStatus.FREQUENT.name(),
            Grade.DIAMOND.name(),
            Gender.MALE.name(),
            LocalDateTime.now());

    int n = 6 - nullableCount;
    int expectedCombinationSize = 0;
    for (int i = 0; i < n; i++) {
      // When
      List<IntroductionSearchCondition> combinations =
          IntroductionSearchConditionCombinator.generateCombinations(base, i);

      // Then
      int r = n - i;
      expectedCombinationSize += calculateCombinationSize(n, r);
      assertThat(combinations)
          .overridingErrorMessage(
              "n = %d, r >= %d 조건에서 조합 개수가 기대값(%d)과 다릅니다! 실제 개수: %d",
              n, r, expectedCombinationSize, combinations.size())
          .hasSize(expectedCombinationSize);
    }
  }

  private int calculateCombinationSize(int n, int r) {
    if (n < 0 || r < 0 || r > n) {
      return 0;
    }

    if (r > n - r) {
      r = n - r;
    }
    int result = 1;
    for (int i = 1; i <= r; i++) {
      result = result * (n - r + i) / i;
    }
    return result;
  }

  @Test
  @DisplayName("조합 생성 시 fieldOptions 순서대로 나이, 도시, 종교, 취미, 흡연 여부, 음주 여부 우선순위로 생성되는지 테스트")
  void generateCombinationsWithPriority() {
    // Given
    IntroductionSearchCondition base = createCondition();

    // When
    List<IntroductionSearchCondition> combinations =
        IntroductionSearchConditionCombinator.generateCombinations(base, 1);

    // Then
    assertThat(combinations).hasSize(7);
    assertCondition(combinations.get(0), base);
    assertFieldOptions(
        combinations.get(1),
        base.getMinAge(),
        base.getMaxAge(),
        base.getCities(),
        base.getReligion(),
        base.getHobbies(),
        base.getSmokingStatus(),
        null);
    assertFieldOptions(
        combinations.get(2),
        base.getMinAge(),
        base.getMaxAge(),
        base.getCities(),
        base.getReligion(),
        base.getHobbies(),
        null,
        base.getDrinkingStatus());
    assertFieldOptions(
        combinations.get(3),
        base.getMinAge(),
        base.getMaxAge(),
        base.getCities(),
        base.getReligion(),
        Set.of(),
        base.getSmokingStatus(),
        base.getDrinkingStatus());
    assertFieldOptions(
        combinations.get(4),
        base.getMinAge(),
        base.getMaxAge(),
        base.getCities(),
        null,
        base.getHobbies(),
        base.getSmokingStatus(),
        base.getDrinkingStatus());
    assertFieldOptions(
        combinations.get(5),
        base.getMinAge(),
        base.getMaxAge(),
        Set.of(),
        base.getReligion(),
        base.getHobbies(),
        base.getSmokingStatus(),
        base.getDrinkingStatus());
    assertFieldOptions(
        combinations.get(6),
        null,
        null,
        base.getCities(),
        base.getReligion(),
        base.getHobbies(),
        base.getSmokingStatus(),
        base.getDrinkingStatus());
  }

  private IntroductionSearchCondition createCondition() {
    return IntroductionSearchCondition.of(
        Set.of(1L, 2L),
        25,
        30,
        Set.of(Hobby.ANIMATION.name()),
        Set.of(City.BUSAN.name()),
        Religion.BUDDHIST.name(),
        SmokingStatus.DAILY.name(),
        DrinkingStatus.FREQUENT.name(),
        Grade.DIAMOND.name(),
        Gender.MALE.name(),
        LocalDateTime.now());
  }

  private void assertCondition(
      IntroductionSearchCondition condition, IntroductionSearchCondition base) {
    assertThat(condition.getCities()).containsAll(base.getCities());
    assertThat(condition.getHobbies()).containsAll(base.getHobbies());
    assertThat(condition.getReligion()).isEqualTo(base.getReligion());
    assertThat(condition.getMinAge()).isEqualTo(base.getMinAge());
    assertThat(condition.getMaxAge()).isEqualTo(base.getMaxAge());
    assertThat(condition.getSmokingStatus()).isEqualTo(base.getSmokingStatus());
    assertThat(condition.getDrinkingStatus()).isEqualTo(base.getDrinkingStatus());
    assertThat(condition.getMemberGrade()).isEqualTo(base.getMemberGrade());
    assertThat(condition.getGender()).isEqualTo(base.getGender());
    assertThat(condition.getExcludedMemberIds()).containsAll(base.getExcludedMemberIds());
    assertThat(condition.getJoinedAfter()).isEqualTo(base.getJoinedAfter());
  }

  private void assertFieldOptions(
      IntroductionSearchCondition condition,
      Integer minAge,
      Integer maxAge,
      Set<String> cities,
      String religion,
      Set<String> hobbies,
      String smokingStatus,
      String drinkingStatus) {
    assertThat(condition.getCities()).containsAll(cities);
    assertThat(condition.getHobbies()).containsAll(hobbies);
    assertThat(condition.getReligion()).isEqualTo(religion);
    assertThat(condition.getMinAge()).isEqualTo(minAge);
    assertThat(condition.getMaxAge()).isEqualTo(maxAge);
    assertThat(condition.getSmokingStatus()).isEqualTo(smokingStatus);
    assertThat(condition.getDrinkingStatus()).isEqualTo(drinkingStatus);
  }
}
