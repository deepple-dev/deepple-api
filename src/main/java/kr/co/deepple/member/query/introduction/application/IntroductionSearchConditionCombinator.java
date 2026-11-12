package kr.co.deepple.member.query.introduction.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import kr.co.deepple.member.command.domain.introduction.vo.AgeRange;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IntroductionSearchConditionCombinator {

  private static final Set<String> EMPTY_SET = Collections.emptySet();

  public static List<IntroductionSearchCondition> generateCombinations(
      IntroductionSearchCondition base, final int maxNoneSelectableCount) {
    List<List<Object>> fieldOptions = getFieldOptions(base);
    final int selectionLimit = calculateSelectionLimit(fieldOptions, maxNoneSelectableCount);
    List<List<Object>> combinations = cartesianProduct(fieldOptions, selectionLimit);
    return getConditions(combinations, base);
  }

  /** fieldOptions 의 순서에 따른 우선순위로 조합을 생성합니다. 나이, 도시, 종교, 취미, 흡연 여부, 음주 여부 순서로 우선순위를 가집니다. */
  private static List<List<Object>> getFieldOptions(IntroductionSearchCondition base) {
    return Arrays.asList(
        choices(AgeRange.of(base.getMinAge(), base.getMaxAge()), null),
        choices(base.getCities(), EMPTY_SET),
        choices(base.getReligion(), null),
        choices(base.getHobbies(), EMPTY_SET),
        choices(base.getSmokingStatus(), null),
        choices(base.getDrinkingStatus(), null));
  }

  private static int calculateSelectionLimit(
      List<List<Object>> fieldOptions, final int maxNoneSelectableCount) {
    int maxSelectable = (int) fieldOptions.stream().filter(opts -> opts.size() > 1).count();
    return Math.max(0, maxSelectable - maxNoneSelectableCount);
  }

  private static <T> List<T> choices(T value, T emptyValue) {
    boolean hasValue =
        (value instanceof Collection) ? !((Collection<?>) value).isEmpty() : value != null;
    return hasValue ? Arrays.asList(value, emptyValue) : Collections.singletonList(emptyValue);
  }

  private static <T> List<List<T>> cartesianProduct(List<List<T>> lists, int selectionLimit) {
    List<List<T>> result = new ArrayList<>();
    accumulate(lists, 0, new ArrayList<>(), 0, selectionLimit, result);
    return result;
  }

  private static <T> void accumulate(
      List<List<T>> lists,
      int depth,
      List<T> current,
      int selectedCount,
      int selectionLimit,
      List<List<T>> result) {

    if (depth == lists.size()) {
      if (selectedCount >= selectionLimit) {
        result.add(new ArrayList<>(current));
      }
      return;
    }
    for (T item : lists.get(depth)) {
      current.add(item);
      boolean isSelected =
          item != null && !(item instanceof Collection && ((Collection<?>) item).isEmpty());
      accumulate(
          lists, depth + 1, current, selectedCount + (isSelected ? 1 : 0), selectionLimit, result);
      current.remove(current.size() - 1);
    }
  }

  @SuppressWarnings("unchecked")
  private static IntroductionSearchCondition toCondition(
      List<Object> values, IntroductionSearchCondition base) {
    final AgeRange ageRange = (AgeRange) values.get(0);
    final Integer minAge = ageRange != null ? ageRange.getMinAge() : null;
    final Integer maxAge = ageRange != null ? ageRange.getMaxAge() : null;
    final Set<String> cities = (Set<String>) values.get(1);
    final String religion = (String) values.get(2);
    final Set<String> hobbies = (Set<String>) values.get(3);
    final String smokingStatus = (String) values.get(4);
    final String drinkingStatus = (String) values.get(5);

    return IntroductionSearchCondition.of(
        base.getExcludedMemberIds(),
        minAge,
        maxAge,
        hobbies,
        cities,
        religion,
        smokingStatus,
        drinkingStatus,
        base.getMemberGrade(),
        base.getGender(),
        base.getJoinedAfter());
  }

  private static List<IntroductionSearchCondition> getConditions(
      List<List<Object>> combinations, IntroductionSearchCondition base) {
    return combinations.stream()
        .map(fieldOption -> IntroductionSearchConditionCombinator.toCondition(fieldOption, base))
        .toList();
  }
}
