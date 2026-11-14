package kr.co.deepple.recommendation.application;

import java.util.List;
import kr.co.deepple.recommendation.application.dto.MemberIntroductionProfileView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberIntroductionProfileViewMapper {

  public static List<MemberIntroductionProfileView> mapWithSameHobbyTag(
      List<MemberIntroductionProfileView> views) {
    return views.stream()
        .map(view ->
            new MemberIntroductionProfileView(
                view.memberId(),
                view.profileImageUrl(),
                view.age(),
                view.nickname(),
                view.city(),
                view.district(),
                List.of(),
                view.mbti(),
                view.religion(),
                view.likeLevel(),
                view.isIntroduced()))
        .toList();
  }

  public static List<MemberIntroductionProfileView> mapWithSameReligionTag(
      List<MemberIntroductionProfileView> views) {
    return views.stream()
        .map(view ->
            new MemberIntroductionProfileView(
                view.memberId(),
                view.profileImageUrl(),
                view.age(),
                view.nickname(),
                view.city(),
                view.district(),
                view.hobbies(),
                view.mbti(),
                null,
                view.likeLevel(),
                view.isIntroduced()))
        .toList();
  }
}
