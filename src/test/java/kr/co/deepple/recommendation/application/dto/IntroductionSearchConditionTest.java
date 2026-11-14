package kr.co.deepple.recommendation.application.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Set;
import kr.co.deepple.member.application.port.in.MemberSearchCondition;
import kr.co.deepple.recommendation.domain.AgeRange;
import kr.co.deepple.recommendation.domain.MemberIdeal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IntroductionSearchConditionTest {

  private final Set<Long> excludedIds = Set.of(1L, 2L);
  private final AgeRange ageRange = AgeRange.of(20, 30);
  private final Set<String> hobbies = Set.of("CAMPING", "ANIMATION");
  private final Set<String> cities = Set.of("SEOUL");
  private final String religion = "CHRISTIAN";
  private final String smokingStatus = "NONE";
  private final String drinkingStatus = "NONE";
  private final String gender = "MALE";

  MemberIdeal getMockedMemberIdeal() {
    MemberIdeal ideal = mock(MemberIdeal.class);
    when(ideal.getAgeRange()).thenReturn(ageRange);
    when(ideal.getHobbies()).thenReturn(hobbies);
    when(ideal.getCities()).thenReturn(cities);
    when(ideal.getReligion()).thenReturn(religion);
    when(ideal.getSmokingStatus()).thenReturn(smokingStatus);
    when(ideal.getDrinkingStatus()).thenReturn(drinkingStatus);
    return ideal;
  }

  void assertCommonFields(IntroductionSearchCondition condition) {
    assertThat(condition.getExcludedMemberIds()).isEqualTo(excludedIds);
    assertThat(condition.getMinAge()).isEqualTo(ageRange.getMinAge());
    assertThat(condition.getMaxAge()).isEqualTo(ageRange.getMaxAge());
    assertThat(condition.getSmokingStatus()).isEqualTo(smokingStatus);
    assertThat(condition.getDrinkingStatus()).isEqualTo(drinkingStatus);
    assertThat(condition.getGender()).isEqualTo(gender);
  }

  @Test
  @DisplayName("ofGrade 메서드 테스트")
  void ofGradeTest() {
    // given
    MemberIdeal ideal = getMockedMemberIdeal();
    String grade = "DIAMOND";

    // when
    IntroductionSearchCondition condition =
        IntroductionSearchCondition.ofGrade(excludedIds, ideal, gender, grade);

    // then
    assertCommonFields(condition);
    assertThat(condition.getHobbies()).isEqualTo(hobbies);
    assertThat(condition.getCities()).isEqualTo(cities);
    assertThat(condition.getReligion()).isEqualTo(religion);
    assertThat(condition.getMemberGrade()).isEqualTo(grade);
    assertThat(condition.getJoinedAfter()).isNull();
  }

  @Test
  @DisplayName("ofHobbies 메서드 테스트")
  void ofHobbiesTest() {
    // given
    MemberIdeal ideal = getMockedMemberIdeal();
    Set<String> memberHobbies = Set.of("CAMPING", "ANIMATION");

    // when
    IntroductionSearchCondition condition =
        IntroductionSearchCondition.ofHobbies(excludedIds, ideal, gender, memberHobbies);

    // then
    assertCommonFields(condition);
    assertThat(condition.getHobbies()).isEqualTo(memberHobbies);
    assertThat(condition.getCities()).isEqualTo(cities);
    assertThat(condition.getReligion()).isEqualTo(religion);
    assertThat(condition.getMemberGrade()).isNull();
    assertThat(condition.getJoinedAfter()).isNull();
  }

  @Test
  @DisplayName("ofReligion 메서드 테스트")
  void ofReligionTest() {
    // given
    MemberIdeal ideal = getMockedMemberIdeal();
    String memberReligion = "NONE";

    // when
    IntroductionSearchCondition condition =
        IntroductionSearchCondition.ofReligion(excludedIds, ideal, gender, memberReligion);

    // then
    assertCommonFields(condition);
    assertThat(condition.getHobbies()).isEqualTo(hobbies);
    assertThat(condition.getCities()).isEqualTo(cities);
    assertThat(condition.getReligion()).isEqualTo(memberReligion);
    assertThat(condition.getMemberGrade()).isNull();
    assertThat(condition.getJoinedAfter()).isNull();
  }

  @Test
  @DisplayName("ofCity 메서드 테스트")
  void ofCityTest() {
    // given
    MemberIdeal ideal = getMockedMemberIdeal();
    String memberCity = "DAEJEON";

    // when
    IntroductionSearchCondition condition =
        IntroductionSearchCondition.ofCity(excludedIds, ideal, gender, memberCity);

    // then
    assertCommonFields(condition);
    assertThat(condition.getHobbies()).isEqualTo(hobbies);
    assertThat(condition.getCities()).isEqualTo(Set.of(memberCity));
    assertThat(condition.getReligion()).isEqualTo(religion);
    assertThat(condition.getMemberGrade()).isNull();
    assertThat(condition.getJoinedAfter()).isNull();
  }

  @Test
  @DisplayName("ofJoinedAfter 메서드 테스트")
  void ofJoinedAfterTest() {
    // given
    MemberIdeal ideal = getMockedMemberIdeal();
    LocalDateTime joinedAfter = LocalDateTime.now();

    // when
    IntroductionSearchCondition condition =
        IntroductionSearchCondition.ofJoinDate(excludedIds, ideal, gender, joinedAfter);

    // then
    assertCommonFields(condition);
    assertThat(condition.getHobbies()).isEqualTo(hobbies);
    assertThat(condition.getCities()).isEqualTo(cities);
    assertThat(condition.getReligion()).isEqualTo(religion);
    assertThat(condition.getMemberGrade()).isNull();
    assertThat(condition.getJoinedAfter()).isEqualTo(joinedAfter);
  }

  @Test
  @DisplayName("회원 검색 조건으로 바꾸면 모든 값이 그대로 옮겨진다.")
  void toMemberSearchConditionTest() {
    LocalDateTime joinedAfter = LocalDateTime.of(2026, 1, 1, 0, 0);
    IntroductionSearchCondition condition =
        IntroductionSearchCondition.of(
            excludedIds,
            20,
            30,
            hobbies,
            cities,
            religion,
            smokingStatus,
            drinkingStatus,
            "DIAMOND",
            gender,
            joinedAfter);

    assertThat(condition.toMemberSearchCondition())
        .isEqualTo(
            new MemberSearchCondition(
                excludedIds,
                20,
                30,
                hobbies,
                cities,
                religion,
                smokingStatus,
                drinkingStatus,
                "DIAMOND",
                gender,
                joinedAfter));
  }
}
