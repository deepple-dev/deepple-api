package kr.co.deepple.member.query.introduction.application;

import java.util.List;
import kr.co.deepple.member.query.introduction.intra.MemberIntroductionProfileQueryResult;
import kr.co.deepple.member.query.member.AgeConverter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberIntroductionProfileViewMapper {

  public static List<MemberIntroductionProfileView> mapWithDefaultTag(
      List<MemberIntroductionProfileQueryResult> profileResults) {
    return profileResults.stream()
        .map(result ->
            new MemberIntroductionProfileView(
                result.memberId(),
                result.profileImageUrl(),
                AgeConverter.toAge(result.yearOfBirth()),
                result.nickname(),
                result.city(),
                result.district(),
                result.hobbies().stream().toList(),
                result.mbti(),
                result.religion(),
                result.likeLevel(),
                result.isIntroduced()))
        .toList();
  }

  public static List<MemberIntroductionProfileView> mapWithSameHobbyTag(
      List<MemberIntroductionProfileQueryResult> profileResults) {
    return profileResults.stream()
        .map(result ->
            new MemberIntroductionProfileView(
                result.memberId(),
                result.profileImageUrl(),
                AgeConverter.toAge(result.yearOfBirth()),
                result.nickname(),
                result.city(),
                result.district(),
                List.of(),
                result.mbti(),
                result.religion(),
                result.likeLevel(),
                result.isIntroduced()))
        .toList();
  }

  public static List<MemberIntroductionProfileView> mapWithSameReligionTag(
      List<MemberIntroductionProfileQueryResult> profileResults) {
    return profileResults.stream()
        .map(result ->
            new MemberIntroductionProfileView(
                result.memberId(),
                result.profileImageUrl(),
                AgeConverter.toAge(result.yearOfBirth()),
                result.nickname(),
                result.city(),
                result.district(),
                result.hobbies().stream().toList(),
                result.mbti(),
                null,
                result.likeLevel(),
                result.isIntroduced()))
        .toList();
  }
}
