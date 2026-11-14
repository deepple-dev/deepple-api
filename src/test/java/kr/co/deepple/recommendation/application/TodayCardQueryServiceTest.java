package kr.co.deepple.recommendation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import kr.co.deepple.recommendation.application.dto.MemberIntroductionProfileView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TodayCardQueryServiceTest {

  @InjectMocks private TodayCardQueryService todayCardQueryService;

  @Mock private MemberIntroductionProfileViewAssembler memberIntroductionProfileViewAssembler;

  @Mock private TodayCardMemberIdFetcher todayCardMemberIdFetcher;

  @Nested
  @DisplayName("findTodayCardMemberIds 메서드 테스트")
  class FindTodayCardMemberIdsTest {

    @Test
    @DisplayName("오늘의 카드 멤버 ID를 반환한다")
    void returnsTodayCardMemberIds() {
      // Given
      long memberId = 1L;
      Set<Long> expectedMemberIds = Set.of(2L, 3L, 4L);
      when(todayCardMemberIdFetcher.fetch(memberId, IntroductionCacheKeyPrefix.TODAY_CARD))
          .thenReturn(expectedMemberIds);

      // When
      Set<Long> result = todayCardQueryService.findTodayCardMemberIds(memberId);

      // Then
      assertThat(result).isEqualTo(expectedMemberIds);
    }
  }

  @Nested
  @DisplayName("findTodayCardIntroductions 메서드 테스트")
  class FindTodayCardIntroductionsTest {

    @Test
    @DisplayName("findTodayCardIntroductions 메서드 테스트")
    void findTodayCardIntroductions() {
      // Given
      long memberId = 1L;
      Set<Long> todayCardMemberIds = Set.of(2L, 3L, 4L);

      List<MemberIntroductionProfileView> views =
          List.of(mock(MemberIntroductionProfileView.class));
      when(memberIntroductionProfileViewAssembler.assemble(memberId, todayCardMemberIds))
          .thenReturn(views);

      // When
      List<MemberIntroductionProfileView> result =
          todayCardQueryService.findTodayCardIntroductions(memberId, todayCardMemberIds);

      // Then
      assertThat(result).containsExactlyElementsOf(views);
    }
  }
}
