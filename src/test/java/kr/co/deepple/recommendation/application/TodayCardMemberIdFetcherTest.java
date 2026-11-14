package kr.co.deepple.recommendation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.application.port.out.MemberCommandRepository;
import kr.co.deepple.member.domain.Gender;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.recommendation.adapter.out.persistence.IntroductionQueryRepository;
import kr.co.deepple.recommendation.adapter.out.persistence.IntroductionRedisRepository;
import kr.co.deepple.recommendation.application.dto.IntroductionSearchCondition;
import kr.co.deepple.recommendation.application.port.out.MemberIdealCommandRepository;
import kr.co.deepple.recommendation.domain.MemberIdeal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TodayCardMemberIdFetcherTest {

  private static final int COMBINATION_NONE_SELECTABLE_SIZE = 2;
  private static final long MALE_LIMIT = 3L;

  @InjectMocks private TodayCardMemberIdFetcher todayCardMemberIdFetcher;

  @Mock private IntroductionQueryRepository introductionQueryRepository;

  @Mock private IntroductionRedisRepository introductionRedisRepository;

  @Mock private MemberCommandRepository memberCommandRepository;

  @Mock private MemberIdealCommandRepository memberIdealCommandRepository;

  private Set<Long> getExcludedMemberIds(Long memberId) {
    Set<Long> matchedRequestedMemberIds = Set.of(memberId, 12L);
    when(introductionQueryRepository.findAllMatchRequestedMemberId(memberId))
        .thenReturn(matchedRequestedMemberIds);
    Set<Long> matchedRequestingMemberIds = Set.of(memberId, 12L);
    when(introductionQueryRepository.findAllMatchRequestingMemberId(memberId))
        .thenReturn(matchedRequestingMemberIds);
    Set<Long> introducedMemberIds = Set.of(memberId, 13L);
    when(introductionQueryRepository.findAllIntroducedMemberId(memberId))
        .thenReturn(introducedMemberIds);
    Set<Long> blockedMemberIds = Set.of(memberId, 14L);
    when(introductionQueryRepository.findAllBlockedMemberId(memberId)).thenReturn(blockedMemberIds);
    Set<Long> blockingMemberIds = Set.of(memberId, 15L);
    when(introductionQueryRepository.findAllBlockingMemberId(memberId))
        .thenReturn(blockingMemberIds);

    Set<Long> excludedMemberIds = new HashSet<>(Set.of(memberId));
    excludedMemberIds.addAll(matchedRequestedMemberIds);
    excludedMemberIds.addAll(matchedRequestingMemberIds);
    excludedMemberIds.addAll(introducedMemberIds);
    excludedMemberIds.addAll(blockedMemberIds);
    excludedMemberIds.addAll(blockingMemberIds);

    return excludedMemberIds;
  }

  @Test
  @DisplayName("캐시된 값이 있으면 캐시된 값을 반환한다.")
  void returnCachedIdsWhenRedisHasValue() {
    // given
    long memberId = 1L;
    IntroductionCacheKeyPrefix cacheKeyPrefix = IntroductionCacheKeyPrefix.TODAY_CARD;
    Set<Long> cachedIds = Set.of(2L, 3L, 4L);
    String key = cacheKeyPrefix.getPrefix() + memberId;
    when(introductionRedisRepository.findIntroductionMemberIds(key)).thenReturn(cachedIds);

    // when
    Set<Long> result = todayCardMemberIdFetcher.fetch(memberId, cacheKeyPrefix);

    // then
    assertThat(result).containsAll(cachedIds);
  }

  @Test
  @DisplayName("캐시된 값이 없고 멤버가 없으면 예외를 던진다.")
  void throwExceptionWhenMemberNotFound() {
    // given
    long memberId = 1L;
    IntroductionCacheKeyPrefix cacheKeyPrefix = IntroductionCacheKeyPrefix.TODAY_CARD;
    when(introductionRedisRepository.findIntroductionMemberIds(
            cacheKeyPrefix.getPrefix() + memberId))
        .thenReturn(Set.of());
    when(memberCommandRepository.findById(memberId)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> todayCardMemberIdFetcher.fetch(memberId, cacheKeyPrefix))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
  }

  @Test
  @DisplayName("캐시된 값이 없고 멤버 이상형이 없으면 예외를 던진다.")
  void throwExceptionWhenMemberIdealNotFound() {
    // given
    long memberId = 1L;
    IntroductionCacheKeyPrefix cacheKeyPrefix = IntroductionCacheKeyPrefix.TODAY_CARD;
    when(introductionRedisRepository.findIntroductionMemberIds(
            cacheKeyPrefix.getPrefix() + memberId))
        .thenReturn(Set.of());

    Member member = MemberFixture.builder().id(memberId).build();
    when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));
    when(memberIdealCommandRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> todayCardMemberIdFetcher.fetch(memberId, cacheKeyPrefix))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_IDEAL_NOT_FOUND);
  }

  @Test
  @DisplayName("캐시된 값이 없고 이상형이 설정되지 않았다면 기본 조건으로 멤버를 조회한다.")
  void fetchWithDefaultConditionWhenIdealNotUpdated() {
    // given
    long memberId = 1L;
    IntroductionCacheKeyPrefix cacheKeyPrefix = IntroductionCacheKeyPrefix.TODAY_CARD;
    when(introductionRedisRepository.findIntroductionMemberIds(
            cacheKeyPrefix.getPrefix() + memberId))
        .thenReturn(Set.of());

    MemberIdeal memberIdeal = mock(MemberIdeal.class);
    when(memberIdealCommandRepository.findByMemberId(memberId))
        .thenReturn(Optional.of(memberIdeal));
    when(memberIdeal.isUpdated()).thenReturn(false);

    Gender memberGender = Gender.MALE;
    Member member = MemberFixture.builder().id(memberId).gender(memberGender).build();
    when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

    Set<Long> excludedMemberIds = getExcludedMemberIds(memberId);

    try (MockedStatic<IntroductionSearchCondition> conditionMockedStatic =
        mockStatic(IntroductionSearchCondition.class)) {
      IntroductionSearchCondition condition = mock(IntroductionSearchCondition.class);
      conditionMockedStatic
          .when(() ->
              IntroductionSearchCondition.ofTodayCardDefault(
                  excludedMemberIds, memberGender.getOpposite(), member))
          .thenReturn(condition);
      Set<Long> expectedIntroducedMemberId = Set.of(2L, 3L, 4L);
      when(introductionQueryRepository.findAllIntroductionMemberId(condition, MALE_LIMIT))
          .thenReturn(expectedIntroducedMemberId);

      // when
      Set<Long> result = todayCardMemberIdFetcher.fetch(memberId, cacheKeyPrefix);

      // then
      assertThat(result).containsAll(expectedIntroducedMemberId);
    }
  }

  @Test
  @DisplayName("캐시된 값이 없고 이상형이 설정되었다면 조합된 조건으로 멤버를 조회한다.")
  void fetchWithCombinationsWhenIdealUpdated() {
    // given
    long memberId = 1L;
    IntroductionCacheKeyPrefix cacheKeyPrefix = IntroductionCacheKeyPrefix.TODAY_CARD;
    when(introductionRedisRepository.findIntroductionMemberIds(
            cacheKeyPrefix.getPrefix() + memberId))
        .thenReturn(Set.of());

    MemberIdeal memberIdeal = mock(MemberIdeal.class);
    when(memberIdealCommandRepository.findByMemberId(memberId))
        .thenReturn(Optional.of(memberIdeal));
    when(memberIdeal.isUpdated()).thenReturn(true);

    Gender memberGender = Gender.MALE;
    Member member = MemberFixture.builder().id(memberId).gender(memberGender).build();
    when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

    Set<Long> excludedMemberIds = getExcludedMemberIds(memberId);

    try (MockedStatic<IntroductionSearchCondition> conditionMockedStatic =
            mockStatic(IntroductionSearchCondition.class);
        MockedStatic<IntroductionSearchConditionCombinator> combinatorMockedStatic =
            mockStatic(IntroductionSearchConditionCombinator.class)) {
      IntroductionSearchCondition baseCondition = mock(IntroductionSearchCondition.class);
      conditionMockedStatic
          .when(() ->
              IntroductionSearchCondition.ofIdeal(
                  excludedMemberIds, memberIdeal, memberGender.getOpposite()))
          .thenReturn(baseCondition);

      IntroductionSearchCondition combinationCondition1 = mock(IntroductionSearchCondition.class);
      IntroductionSearchCondition combinationCondition2 = mock(IntroductionSearchCondition.class);
      IntroductionSearchCondition combinationCondition3 = mock(IntroductionSearchCondition.class);
      List<IntroductionSearchCondition> combinationConditions =
          List.of(combinationCondition1, combinationCondition2, combinationCondition3);

      combinatorMockedStatic
          .when(() ->
              IntroductionSearchConditionCombinator.generateCombinations(
                  baseCondition, COMBINATION_NONE_SELECTABLE_SIZE))
          .thenReturn(combinationConditions);

      Set<Long> expectedIntroducedMemberId = Set.of(2L, 3L, 4L);
      when(introductionQueryRepository.findAllIntroductionMemberId(
              combinationCondition1, MALE_LIMIT))
          .thenReturn(Set.of(2L));
      when(introductionQueryRepository.findAllIntroductionMemberId(
              combinationCondition2, MALE_LIMIT - 1))
          .thenReturn(Set.of(3L, 4L));

      // when
      Set<Long> result = todayCardMemberIdFetcher.fetch(memberId, cacheKeyPrefix);

      // then
      assertThat(result).containsAll(expectedIntroducedMemberId);
    }
  }
}
