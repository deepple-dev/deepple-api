package kr.co.deepple.recommendation.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import kr.co.deepple.recommendation.application.dto.MemberIntroductionProfileView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberIntroductionProfileViewMapperTest {

  private final MemberIntroductionProfileView view =
      new MemberIntroductionProfileView(
          1L,
          "imageUrl",
          30,
          "nickname",
          "SEOUL",
          "GANGNAM_GU",
          List.of("CAMPING", "WINE"),
          "INFJ",
          "BUDDHIST",
          "INTERESTED",
          true);

  @Test
  @DisplayName("취미가 같은 이성 소개는 취미를 비우고 나머지 값은 그대로 둔다.")
  void mapWithSameHobbyTagTest() {
    List<MemberIntroductionProfileView> views =
        MemberIntroductionProfileViewMapper.mapWithSameHobbyTag(List.of(view));

    assertThat(views)
        .containsExactly(
            new MemberIntroductionProfileView(
                1L,
                "imageUrl",
                30,
                "nickname",
                "SEOUL",
                "GANGNAM_GU",
                List.of(),
                "INFJ",
                "BUDDHIST",
                "INTERESTED",
                true));
  }

  @Test
  @DisplayName("종교가 같은 이성 소개는 종교를 비우고 나머지 값은 그대로 둔다.")
  void mapWithSameReligionTagTest() {
    List<MemberIntroductionProfileView> views =
        MemberIntroductionProfileViewMapper.mapWithSameReligionTag(List.of(view));

    assertThat(views)
        .containsExactly(
            new MemberIntroductionProfileView(
                1L,
                "imageUrl",
                30,
                "nickname",
                "SEOUL",
                "GANGNAM_GU",
                List.of("CAMPING", "WINE"),
                "INFJ",
                null,
                "INTERESTED",
                true));
  }
}
