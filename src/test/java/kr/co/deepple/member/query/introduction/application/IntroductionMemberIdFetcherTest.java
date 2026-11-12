package kr.co.deepple.member.query.introduction.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.introduction.MemberIdeal;
import kr.co.deepple.member.command.domain.introduction.MemberIdealCommandRepository;
import kr.co.deepple.member.command.domain.member.Gender;
import kr.co.deepple.member.command.domain.member.Grade;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.member.fixture.MemberIdealFixture;
import kr.co.deepple.member.query.introduction.intra.IntroductionQueryRepository;
import kr.co.deepple.member.query.introduction.intra.IntroductionRedisRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IntroductionMemberIdFetcherTest {

  @InjectMocks private IntroductionMemberIdFetcher introductionMemberIdFetcher;

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
  @DisplayName("Redis에 저장된 값이 있을 때 fetch 메서드가 캐시된 값을 반환한다.")
  void returnCachedIdsWhenRedisHasValue() {
    // given
    long memberId = 1L;
    IntroductionCacheKeyPrefix introductionCacheKeyPrefix = IntroductionCacheKeyPrefix.DIAMOND;
    Set<Long> cachedIds = Set.of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);
    String key = introductionCacheKeyPrefix.getPrefix() + memberId;
    when(introductionRedisRepository.findIntroductionMemberIds(key)).thenReturn(cachedIds);

    // when
    Set<Long> result =
        introductionMemberIdFetcher.fetch(
            memberId,
            introductionCacheKeyPrefix,
            null,
            (excludedMemberIds, memberIdeal, member, criteria) -> null);

    // then
    assertThat(result).isEqualTo(cachedIds);
  }

  @Test
  @DisplayName("Redis에 저장된 값이 없을 때 fetch 메서드가 DB 저장된 값을 반환한다.")
  void returnDBValueWhenRedisHasNoValue() {
    // given
    long memberId = 1L;
    IntroductionCacheKeyPrefix introductionCacheKeyPrefix = IntroductionCacheKeyPrefix.DIAMOND;
    Set<Long> savedIds = Set.of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);
    String key = introductionCacheKeyPrefix.getPrefix() + memberId;
    when(introductionRedisRepository.findIntroductionMemberIds(key)).thenReturn(Set.of());

    Set<Long> excludedMemberIds = getExcludedMemberIds(memberId);

    MemberIdeal memberIdeal = MemberIdealFixture.builder().memberId(memberId).build();
    when(memberIdealCommandRepository.findByMemberId(memberId))
        .thenReturn(Optional.of(memberIdeal));
    Member member = MemberFixture.builder().id(memberId).build();
    when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

    IntroductionSearchCondition dummyCondition =
        IntroductionSearchCondition.ofGrade(
            excludedMemberIds, memberIdeal, Gender.MALE, Grade.DIAMOND);
    IntroductionMemberIdFetcher.IntroductionConditionSupplier<Grade> supplier =
        (excluded, ideal, m, grade) -> dummyCondition;

    when(introductionQueryRepository.findAllIntroductionMemberId(dummyCondition, 10))
        .thenReturn(savedIds);

    doNothing()
        .when(introductionRedisRepository)
        .saveIntroductionMemberIds(anyString(), eq(savedIds), any(Date.class));

    // when
    Set<Long> result =
        introductionMemberIdFetcher.fetch(
            memberId, IntroductionCacheKeyPrefix.DIAMOND, Grade.DIAMOND, supplier);

    // then
    assertThat(result).isEqualTo(savedIds);
    verify(introductionRedisRepository)
        .saveIntroductionMemberIds(eq(key), eq(savedIds), any(Date.class));
  }

  @Test
  @DisplayName("Redis에 저장된 값이 없고 멤버가 없으면 예외를 던진다")
  void throwExceptionWhenMemberNotFound() {
    // given
    long memberId = 1L;
    IntroductionCacheKeyPrefix introductionCacheKeyPrefix = IntroductionCacheKeyPrefix.DIAMOND;
    String key = introductionCacheKeyPrefix.getPrefix() + memberId;
    when(introductionRedisRepository.findIntroductionMemberIds(key)).thenReturn(Set.of());

    Set<Long> excludedMemberIds = getExcludedMemberIds(memberId);

    MemberIdeal memberIdeal = MemberIdealFixture.builder().memberId(memberId).build();
    when(memberIdealCommandRepository.findByMemberId(memberId))
        .thenReturn(Optional.of(memberIdeal));
    when(memberCommandRepository.findById(memberId)).thenReturn(Optional.empty());

    IntroductionSearchCondition dummyCondition =
        IntroductionSearchCondition.ofGrade(
            excludedMemberIds, memberIdeal, Gender.MALE, Grade.DIAMOND);
    IntroductionMemberIdFetcher.IntroductionConditionSupplier<Grade> supplier =
        (excluded, ideal, m, grade) -> dummyCondition;

    // when && then
    assertThatThrownBy(() ->
            introductionMemberIdFetcher.fetch(
                memberId, IntroductionCacheKeyPrefix.DIAMOND, Grade.DIAMOND, supplier))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
  }

  @Test
  @DisplayName("Redis에 저장된 값이 없고 멤버 이상형이 없으면 예외를 던진다")
  void throwExceptionWhenMemberIdealNotFound() {
    // given
    long memberId = 1L;
    IntroductionCacheKeyPrefix introductionCacheKeyPrefix = IntroductionCacheKeyPrefix.DIAMOND;
    String key = introductionCacheKeyPrefix.getPrefix() + memberId;
    when(introductionRedisRepository.findIntroductionMemberIds(key)).thenReturn(Set.of());

    Set<Long> excludedMemberIds = getExcludedMemberIds(memberId);

    when(memberIdealCommandRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

    MemberIdeal memberIdeal = MemberIdealFixture.builder().memberId(memberId).build();
    IntroductionSearchCondition dummyCondition =
        IntroductionSearchCondition.ofGrade(
            excludedMemberIds, memberIdeal, Gender.MALE, Grade.DIAMOND);
    IntroductionMemberIdFetcher.IntroductionConditionSupplier<Grade> supplier =
        (excluded, ideal, m, grade) -> dummyCondition;

    // when && then
    assertThatThrownBy(() ->
            introductionMemberIdFetcher.fetch(
                memberId, IntroductionCacheKeyPrefix.DIAMOND, Grade.DIAMOND, supplier))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_IDEAL_NOT_FOUND);
  }
}
