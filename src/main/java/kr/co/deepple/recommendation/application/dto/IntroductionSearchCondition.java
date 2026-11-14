package kr.co.deepple.recommendation.application.dto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import kr.co.deepple.member.application.port.in.MemberSummary;
import kr.co.deepple.recommendation.domain.AgeRange;
import kr.co.deepple.recommendation.domain.MemberIdeal;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class IntroductionSearchCondition {

  private static final Integer TODAY_CARD_AGE_DEFAULT_RANGE = 3;
  private static final String ACTIVE = "ACTIVE";

  private final Set<Long> excludedMemberIds;
  private final Integer minAge;
  private final Integer maxAge;
  private final Set<String> hobbies;
  private final Set<String> cities;
  private final String religion;
  private final String smokingStatus;
  private final String drinkingStatus;
  private final String memberGrade;
  private final String gender;
  private final LocalDateTime joinedAfter;
  private final String activityStatus = ACTIVE;

  private IntroductionSearchCondition(
      Set<Long> excludedMemberIds,
      AgeRange ageRange,
      Set<String> hobbies,
      Set<String> cities,
      String religion,
      String smokingStatus,
      String drinkingStatus,
      String memberGrade,
      @NonNull String gender,
      LocalDateTime joinedAfter) {
    this(
        excludedMemberIds,
        (ageRange != null) ? ageRange.getMinAge() : null,
        (ageRange != null) ? ageRange.getMaxAge() : null,
        new HashSet<>(hobbies),
        new HashSet<>(cities),
        religion,
        smokingStatus,
        drinkingStatus,
        memberGrade,
        gender,
        joinedAfter);
  }

  private IntroductionSearchCondition(
      Set<Long> excludedMemberIds,
      Integer minAge,
      Integer maxAge,
      Set<String> hobbies,
      Set<String> cities,
      String religion,
      String smokingStatus,
      String drinkingStatus,
      String memberGrade,
      @NonNull String gender,
      LocalDateTime joinedAfter) {
    this.excludedMemberIds = excludedMemberIds;
    this.minAge = minAge;
    this.maxAge = maxAge;
    this.hobbies = hobbies;
    this.cities = cities;
    this.religion = religion;
    this.smokingStatus = smokingStatus;
    this.drinkingStatus = drinkingStatus;
    this.memberGrade = memberGrade;
    this.gender = gender;
    this.joinedAfter = joinedAfter;
  }

  public static IntroductionSearchCondition ofGrade(
      Set<Long> excludedMemberIds, MemberIdeal memberIdeal, String gender, String grade) {
    return new IntroductionSearchCondition(
        excludedMemberIds,
        memberIdeal.getAgeRange(),
        memberIdeal.getHobbies(),
        memberIdeal.getCities(),
        memberIdeal.getReligion(),
        memberIdeal.getSmokingStatus(),
        memberIdeal.getDrinkingStatus(),
        grade,
        gender,
        null);
  }

  public static IntroductionSearchCondition ofHobbies(
      Set<Long> excludedMemberIds, MemberIdeal memberIdeal, String gender, Set<String> hobbies) {
    return new IntroductionSearchCondition(
        excludedMemberIds,
        memberIdeal.getAgeRange(),
        hobbies,
        memberIdeal.getCities(),
        memberIdeal.getReligion(),
        memberIdeal.getSmokingStatus(),
        memberIdeal.getDrinkingStatus(),
        null,
        gender,
        null);
  }

  public static IntroductionSearchCondition ofReligion(
      Set<Long> excludedMemberIds, MemberIdeal memberIdeal, String gender, String religion) {
    return new IntroductionSearchCondition(
        excludedMemberIds,
        memberIdeal.getAgeRange(),
        memberIdeal.getHobbies(),
        memberIdeal.getCities(),
        religion,
        memberIdeal.getSmokingStatus(),
        memberIdeal.getDrinkingStatus(),
        null,
        gender,
        null);
  }

  public static IntroductionSearchCondition ofCity(
      Set<Long> excludedMemberIds, MemberIdeal memberIdeal, String gender, String city) {
    return new IntroductionSearchCondition(
        excludedMemberIds,
        memberIdeal.getAgeRange(),
        memberIdeal.getHobbies(),
        Set.of(city),
        memberIdeal.getReligion(),
        memberIdeal.getSmokingStatus(),
        memberIdeal.getDrinkingStatus(),
        null,
        gender,
        null);
  }

  public static IntroductionSearchCondition ofJoinDate(
      Set<Long> excludedMemberIds,
      MemberIdeal memberIdeal,
      String gender,
      LocalDateTime joinedAfter) {
    return new IntroductionSearchCondition(
        excludedMemberIds,
        memberIdeal.getAgeRange(),
        memberIdeal.getHobbies(),
        memberIdeal.getCities(),
        memberIdeal.getReligion(),
        memberIdeal.getSmokingStatus(),
        memberIdeal.getDrinkingStatus(),
        null,
        gender,
        joinedAfter);
  }

  public static IntroductionSearchCondition ofIdeal(
      Set<Long> excludedMemberIds, MemberIdeal memberIdeal, String gender) {
    return new IntroductionSearchCondition(
        excludedMemberIds,
        memberIdeal.getAgeRange(),
        memberIdeal.getHobbies(),
        memberIdeal.getCities(),
        memberIdeal.getReligion(),
        memberIdeal.getSmokingStatus(),
        memberIdeal.getDrinkingStatus(),
        null,
        gender,
        null);
  }

  public static IntroductionSearchCondition ofTodayCardDefault(
      Set<Long> excludedMemberIds, String gender, MemberSummary member) {
    return new IntroductionSearchCondition(
        excludedMemberIds,
        AgeRange.ofRange(member.age(), TODAY_CARD_AGE_DEFAULT_RANGE),
        Set.of(),
        Set.of(member.city()),
        null,
        null,
        null,
        member.grade(),
        gender,
        null);
  }

  public static IntroductionSearchCondition of(
      Set<Long> excludedMemberIds,
      Integer minAge,
      Integer maxAge,
      Set<String> hobbies,
      Set<String> cities,
      String religion,
      String smokingStatus,
      String drinkingStatus,
      String memberGrade,
      String gender,
      LocalDateTime joinedAfter) {
    return new IntroductionSearchCondition(
        excludedMemberIds,
        minAge,
        maxAge,
        hobbies,
        cities,
        religion,
        smokingStatus,
        drinkingStatus,
        memberGrade,
        gender,
        joinedAfter);
  }
}
