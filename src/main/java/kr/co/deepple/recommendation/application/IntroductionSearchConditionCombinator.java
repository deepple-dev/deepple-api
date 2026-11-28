package kr.co.deepple.recommendation.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import kr.co.deepple.recommendation.application.dto.IntroductionSearchCondition;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IntroductionSearchConditionCombinator {

  private static final Set<String> EMPTY_SET = Collections.emptySet();

  public static List<IntroductionSearchCondition> generateCombinations(
      IntroductionSearchCondition base, int maxNoneSelectableCount) {
    List<List<Object>> fieldOptions = getFieldOptions(base);
    int selectionLimit = calculateSelectionLimit(fieldOptions, maxNoneSelectableCount);
    List<List<Object>> combinations = cartesianProduct(fieldOptions, selectionLimit);
    return getConditions(combinations, base);
  }

  /** fieldOptions 의 순서에 따른 우선순위로 조합을 생성합니다. 나이, 도시, 종교, 취미, 흡연 여부, 음주 여부 순서로 우선순위를 가집니다. */
  private static List<List<Object>> getFieldOptions(IntroductionSearchCondition base) {
    return Arrays.asList(
        choices(Ages.of(base.getMinAge(), base.getMaxAge()), null),
        choices(base.getCities(), EMPTY_SET),
        choices(base.getReligion(), null),
        choices(base.getHobbies(), EMPTY_SET),
        choices(base.getSmokingStatus(), null),
        choices(base.getDrinkingStatus(), null));
  }

  private static int calculateSelectionLimit(
      List<List<Object>> fieldOptions, int maxNoneSelectableCount) {
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
    Ages ages = (Ages) values.get(0);
    Integer minAge = ages != null ? ages.minAge() : null;
    Integer maxAge = ages != null ? ages.maxAge() : null;
    Set<String> cities = (Set<String>) values.get(1);
    String religion = (String) values.get(2);
    Set<String> hobbies = (Set<String>) values.get(3);
    String smokingStatus = (String) values.get(4);
    String drinkingStatus = (String) values.get(5);

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

  /**
   * 저장된 나이 범위를 그대로 옮긴다. {@code AgeRange}로 다시 만들면 범위 검사를 다시 해서, 허용 범위가 좁아지기 전에 저장된 값(예: 46)이 있는 회원의
   * 추천 전체가 실패한다.
   */
  private record Ages(Integer minAge, Integer maxAge) {

    private static Ages of(Integer minAge, Integer maxAge) {
      return minAge == null && maxAge == null ? null : new Ages(minAge, maxAge);
    }
  }
}
