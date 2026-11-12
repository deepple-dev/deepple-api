package kr.co.deepple.member.query.introduction.application;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.member.command.domain.introduction.MemberIdeal;
import kr.co.deepple.member.command.domain.introduction.vo.AgeRange;
import kr.co.deepple.member.command.domain.member.ActivityStatus;
import kr.co.deepple.member.command.domain.member.City;
import kr.co.deepple.member.command.domain.member.DrinkingStatus;
import kr.co.deepple.member.command.domain.member.Gender;
import kr.co.deepple.member.command.domain.member.Grade;
import kr.co.deepple.member.command.domain.member.Hobby;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.Religion;
import kr.co.deepple.member.command.domain.member.SmokingStatus;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class IntroductionSearchCondition {

  private static final Integer TODAY_CARD_AGE_DEFAULT_RANGE = 3;

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
  private final String activityStatus = ActivityStatus.ACTIVE.name();

  private IntroductionSearchCondition(
      Set<Long> excludedMemberIds,
      AgeRange ageRange,
      Set<Hobby> hobbies,
      Set<City> cities,
      Religion religion,
      SmokingStatus smokingStatus,
      DrinkingStatus drinkingStatus,
      Grade memberGrade,
      @NonNull Gender gender,
      LocalDateTime joinedAfter) {
    this.excludedMemberIds = excludedMemberIds;
    this.minAge = (ageRange != null) ? ageRange.getMinAge() : null;
    this.maxAge = (ageRange != null) ? ageRange.getMaxAge() : null;
    this.hobbies = hobbies.stream().map(Hobby::name).collect(Collectors.toSet());
    this.cities = cities.stream().map(City::name).collect(Collectors.toSet());
    this.religion = (religion != null) ? religion.name() : null;
    this.smokingStatus = (smokingStatus != null) ? smokingStatus.name() : null;
    this.drinkingStatus = (drinkingStatus != null) ? drinkingStatus.name() : null;
    this.memberGrade = (memberGrade != null) ? memberGrade.name() : null;
    this.gender = gender.name();
    this.joinedAfter = joinedAfter;
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
      Set<Long> excludedMemberIds, MemberIdeal memberIdeal, Gender gender, Grade grade) {
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
      Set<Long> excludedMemberIds, MemberIdeal memberIdeal, Gender gender, Set<Hobby> hobbies) {
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
      Set<Long> excludedMemberIds, MemberIdeal memberIdeal, Gender gender, Religion religion) {
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
      Set<Long> excludedMemberIds, MemberIdeal memberIdeal, Gender gender, City city) {
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
      Gender gender,
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
      Set<Long> excludedMemberIds, MemberIdeal memberIdeal, Gender gender) {
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
      Set<Long> excludedMemberIds, Gender gender, Member member) {
    return new IntroductionSearchCondition(
        excludedMemberIds,
        AgeRange.ofRange(
            member.getProfile().getYearOfBirth().getAge(), TODAY_CARD_AGE_DEFAULT_RANGE),
        Set.of(),
        Set.of(member.getProfile().getRegion().getCity()),
        null,
        null,
        null,
        member.getGrade(),
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
