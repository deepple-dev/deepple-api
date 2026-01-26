package deepple.deepple.member.query.introduction.application;

import deepple.deepple.member.query.introduction.intra.IntroductionQueryRepository;
import deepple.deepple.member.query.introduction.intra.MemberIntroductionProfileQueryResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodayCardQueryServiceTest {

    @InjectMocks
    private TodayCardQueryService todayCardQueryService;

    @Mock
    private IntroductionQueryRepository introductionQueryRepository;

    @Mock
    private TodayCardMemberIdFetcher todayCardMemberIdFetcher;

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

            List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults = List.of(
                mock(MemberIntroductionProfileQueryResult.class));
            when(introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(memberId,
                todayCardMemberIds))
                .thenReturn(memberIntroductionProfileQueryResults);

            try (MockedStatic<MemberIntroductionProfileViewMapper> mockedMapper = mockStatic(
                MemberIntroductionProfileViewMapper.class)) {
                List<MemberIntroductionProfileView> views = List.of(mock(MemberIntroductionProfileView.class));
                mockedMapper.when(
                        () -> MemberIntroductionProfileViewMapper.mapWithDefaultTag(memberIntroductionProfileQueryResults))
                    .thenReturn(views);

                // When
                List<MemberIntroductionProfileView> result = todayCardQueryService.findTodayCardIntroductions(memberId,
                    todayCardMemberIds);

                // Then
                assertThat(result).hasSize(1);
                assertThat(result).containsExactlyElementsOf(views);
            }
        }
    }
}
