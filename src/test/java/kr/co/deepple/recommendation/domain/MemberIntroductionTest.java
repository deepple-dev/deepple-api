package kr.co.deepple.recommendation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import kr.co.deepple.common.exception.DeeppleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MemberIntroductionTest {

  @ParameterizedTest
  @ValueSource(strings = {"memberId", "introducedMemberId", "content"})
  @DisplayName("null 값을 가지고 생성하면 예외를 던진다.")
  void throwsExceptionWhenNullValue(String nullParam) {
    // given
    Long memberId = nullParam.equals("memberId") ? null : 1L;
    Long introducedMemberId = nullParam.equals("introducedMemberId") ? null : 2L;
    IntroductionType type = nullParam.equals("content") ? null : mock(IntroductionType.class);

    // when & then
    assertThatThrownBy(() -> MemberIntroduction.of(memberId, introducedMemberId, type))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("소개를 생성한다.")
  void createIntroduction() {
    // given
    long memberId = 1L;
    long introducedMemberId = 2L;

    // when
    MemberIntroduction memberIntroduction =
        MemberIntroduction.of(memberId, introducedMemberId, IntroductionType.DIAMOND_GRADE);

    // then
    assertThat(memberIntroduction.getMemberId()).isEqualTo(memberId);
    assertThat(memberIntroduction.getIntroducedMemberId()).isEqualTo(introducedMemberId);
    assertThat(memberIntroduction.getType()).isEqualTo(IntroductionType.DIAMOND_GRADE);
  }

  @Test
  @DisplayName("소개 멤버 id가 같으면 예외를 던진다.")
  void throwsExceptionWhenMemberIdEqualsIntroducedMemberId() {
    // given
    long memberId = 1L;
    long introducedMemberId = 1L;
    IntroductionType type = mock(IntroductionType.class);

    // when & then
    assertThatThrownBy(() -> MemberIntroduction.of(memberId, introducedMemberId, type))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", RecommendationErrorCode.SELF_MEMBER_INTRODUCTION);
  }
}
