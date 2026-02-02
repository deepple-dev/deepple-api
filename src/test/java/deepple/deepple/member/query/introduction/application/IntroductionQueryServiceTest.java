package deepple.deepple.member.query.introduction.application;

import deepple.deepple.member.command.domain.member.City;
import deepple.deepple.member.command.domain.member.Grade;
import deepple.deepple.member.command.domain.member.Religion;
import deepple.deepple.member.query.introduction.intra.IntroductionQueryRepository;
import deepple.deepple.member.query.introduction.intra.MemberIntroductionProfileQueryResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntroductionQueryServiceTest {

    @InjectMocks
    private IntroductionQueryService introductionQueryService;

    @Mock
    private IntroductionQueryRepository introductionQueryRepository;

    @Mock
    private IntroductionMemberIdFetcher introductionMemberIdFetcher;

    @Test
    @DisplayName("findDiamondGradeIntroductions 메서드 테스트")
    void findDiamondGradeIntroductionsTest() {
        // given
        long memberId = 1L;
        Set<Long> introductionMemberIds = Set.of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);
        when(introductionMemberIdFetcher.fetch(
            eq(memberId),
            eq(IntroductionCacheKeyPrefix.DIAMOND),
            eq(Grade.DIAMOND),
            ArgumentMatchers.<IntroductionMemberIdFetcher.IntroductionConditionSupplier<Grade>>any()
        )).thenReturn(introductionMemberIds);
        List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults = List.of();
        when(introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(memberId,
            introductionMemberIds))
            .thenReturn(memberIntroductionProfileQueryResults);
        List<MemberIntroductionProfileView> expectedResult = List.of();

        // when && then
        try (MockedStatic<MemberIntroductionProfileViewMapper> mockedStatic = mockStatic(
            MemberIntroductionProfileViewMapper.class)) {
            mockedStatic.when(
                    () -> MemberIntroductionProfileViewMapper.mapWithDefaultTag(memberIntroductionProfileQueryResults))
                .thenReturn(expectedResult);
            List<MemberIntroductionProfileView> result = introductionQueryService.findDiamondGradeIntroductions(
                memberId);
            assertThat(result).isEqualTo(expectedResult);
        }
    }

    @Test
    @DisplayName("findSameHobbyIntroductions 메서드 테스트")
    void findSameHobbyIntroductionsTest() {
        // given
        long memberId = 1L;
        Set<Long> introductionMemberIds = Set.of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);
        when(introductionMemberIdFetcher.fetch(
            eq(memberId),
            eq(IntroductionCacheKeyPrefix.SAME_HOBBY),
            isNull(),
            ArgumentMatchers.<IntroductionMemberIdFetcher.IntroductionConditionSupplier<Set>>any()
        )).thenReturn(introductionMemberIds);
        List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults = List.of();
        when(introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(memberId,
            introductionMemberIds))
            .thenReturn(memberIntroductionProfileQueryResults);
        List<MemberIntroductionProfileView> expectedResult = List.of();

        // when && then
        try (MockedStatic<MemberIntroductionProfileViewMapper> mockedStatic = mockStatic(
            MemberIntroductionProfileViewMapper.class)) {
            mockedStatic.when(
                    () -> MemberIntroductionProfileViewMapper.mapWithSameHobbyTag(memberIntroductionProfileQueryResults))
                .thenReturn(expectedResult);
            List<MemberIntroductionProfileView> result = introductionQueryService.findSameHobbyIntroductions(memberId);
            assertThat(result).isEqualTo(expectedResult);
        }
    }

    @Test
    @DisplayName("findSameReligionIntroductions 메서드 테스트")
    void findSameReligionIntroductionsTest() {
        // given
        long memberId = 1L;
        Set<Long> introductionMemberIds = Set.of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);
        when(introductionMemberIdFetcher.fetch(
            eq(memberId),
            eq(IntroductionCacheKeyPrefix.SAME_RELIGION),
            isNull(),
            ArgumentMatchers.<IntroductionMemberIdFetcher.IntroductionConditionSupplier<Religion>>any()
        )).thenReturn(introductionMemberIds);
        List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults = List.of();
        when(introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(memberId,
            introductionMemberIds))
            .thenReturn(memberIntroductionProfileQueryResults);
        List<MemberIntroductionProfileView> expectedResult = List.of();

        // when && then
        try (MockedStatic<MemberIntroductionProfileViewMapper> mockedStatic = mockStatic(
            MemberIntroductionProfileViewMapper.class)) {
            mockedStatic.when(
                    () -> MemberIntroductionProfileViewMapper.mapWithSameReligionTag(memberIntroductionProfileQueryResults))
                .thenReturn(expectedResult);
            List<MemberIntroductionProfileView> result = introductionQueryService.findSameReligionIntroductions(
                memberId);
            assertThat(result).isEqualTo(expectedResult);
        }
    }

    @Test
    @DisplayName("findSameCityIntroductions 메서드 테스트")
    void findSameCityIntroductionsTest() {
        // given
        long memberId = 1L;
        Set<Long> introductionMemberIds = Set.of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);
        when(introductionMemberIdFetcher.fetch(
            eq(memberId),
            eq(IntroductionCacheKeyPrefix.SAME_CITY),
            isNull(),
            ArgumentMatchers.<IntroductionMemberIdFetcher.IntroductionConditionSupplier<City>>any()
        )).thenReturn(introductionMemberIds);
        List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults = List.of();
        when(introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(memberId,
            introductionMemberIds))
            .thenReturn(memberIntroductionProfileQueryResults);
        List<MemberIntroductionProfileView> expectedResult = List.of();

        // when && then
        try (MockedStatic<MemberIntroductionProfileViewMapper> mockedStatic = mockStatic(
            MemberIntroductionProfileViewMapper.class)) {
            mockedStatic.when(
                    () -> MemberIntroductionProfileViewMapper.mapWithDefaultTag(memberIntroductionProfileQueryResults))
                .thenReturn(expectedResult);
            List<MemberIntroductionProfileView> result = introductionQueryService.findSameCityIntroductions(memberId);
            assertThat(result).isEqualTo(expectedResult);
        }
    }

    @Test
    @DisplayName("findRecentlyJoinedIntroductions 메서드 테스트")
    void findRecentlyJoinedIntroductionsTest() {
        // given
        long memberId = 1L;
        Set<Long> introductionMemberIds = Set.of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);
        when(introductionMemberIdFetcher.fetch(
            eq(memberId),
            eq(IntroductionCacheKeyPrefix.RECENTLY_JOINED),
            isNull(),
            ArgumentMatchers.<IntroductionMemberIdFetcher.IntroductionConditionSupplier<LocalDateTime>>any()
        )).thenReturn(introductionMemberIds);
        List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults = List.of();
        when(introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(memberId,
            introductionMemberIds))
            .thenReturn(memberIntroductionProfileQueryResults);
        List<MemberIntroductionProfileView> expectedResult = List.of();

        // when && then
        try (MockedStatic<MemberIntroductionProfileViewMapper> mockedStatic = mockStatic(
            MemberIntroductionProfileViewMapper.class)) {
            mockedStatic.when(
                    () -> MemberIntroductionProfileViewMapper.mapWithDefaultTag(memberIntroductionProfileQueryResults))
                .thenReturn(expectedResult);
            List<MemberIntroductionProfileView> result = introductionQueryService.findRecentlyJoinedIntroductions(
                memberId);
            assertThat(result).isEqualTo(expectedResult);
        }
    }
}
