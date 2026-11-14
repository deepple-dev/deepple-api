package kr.co.deepple.recommendation.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import kr.co.deepple.block.application.port.in.FindBlockedMemberIdsUseCase;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.match.application.port.in.FindMatchCounterpartIdsUseCase;
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import kr.co.deepple.member.application.port.in.MemberSummary;
import kr.co.deepple.member.application.port.in.SearchIntroducibleMemberIdsUseCase;
import kr.co.deepple.recommendation.application.dto.IntroductionSearchCondition;
import kr.co.deepple.recommendation.application.port.out.IntroductionCacheRepository;
import kr.co.deepple.recommendation.application.port.out.IntroductionQueryRepository;
import kr.co.deepple.recommendation.application.port.out.MemberIdealRepository;
import kr.co.deepple.recommendation.domain.MemberIdeal;
import kr.co.deepple.recommendation.domain.RecommendationErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IntroductionMemberIdFetcher {

  private static final int FETCH_LIMIT = 10;

  private final IntroductionQueryRepository introductionQueryRepository;
  private final IntroductionCacheRepository introductionCacheRepository;
  private final FindMemberSummaryUseCase findMemberSummaryUseCase;
  private final MemberIdealRepository memberIdealRepository;
  private final SearchIntroducibleMemberIdsUseCase searchIntroducibleMemberIdsUseCase;
  private final FindMatchCounterpartIdsUseCase findMatchCounterpartIdsUseCase;
  private final FindBlockedMemberIdsUseCase findBlockedMemberIdsUseCase;

  public <T> Set<Long> fetch(
      long memberId,
      IntroductionCacheKeyPrefix cacheKeyPrefix,
      T criteria,
      IntroductionConditionSupplier<T> supplier) {
    String cacheKey = buildKey(cacheKeyPrefix, memberId);
    Set<Long> cachedIds = introductionCacheRepository.findIntroductionMemberIds(cacheKey);
    if (!cachedIds.isEmpty()) {
      return cachedIds;
    }
    Set<Long> excludedMemberIds = findExcludedMemberIds(memberId);
    MemberIdeal memberIdeal =
        memberIdealRepository
            .findByMemberId(memberId)
            .orElseThrow(() ->
                new DeeppleException(
                    RecommendationErrorCode.MEMBER_IDEAL_NOT_FOUND, "memberId=" + memberId));
    MemberSummary member = findMemberSummaryUseCase.getMemberSummary(memberId);
    IntroductionSearchCondition condition =
        supplier.get(excludedMemberIds, memberIdeal, member, criteria);
    Set<Long> introductionMemberIds =
        searchIntroducibleMemberIdsUseCase.searchMemberIds(
            condition.toMemberSearchCondition(), FETCH_LIMIT);
    introductionCacheRepository.saveIntroductionMemberIds(
        cacheKey, introductionMemberIds, getExpireAt());
    return introductionMemberIds;
  }

  private Set<Long> findExcludedMemberIds(long memberId) {
    Set<Long> excludedMemberIds = new HashSet<>();
    excludedMemberIds.add(memberId);
    excludedMemberIds.addAll(findMatchCounterpartIdsUseCase.findMatchCounterpartIds(memberId));
    excludedMemberIds.addAll(introductionQueryRepository.findAllIntroducedMemberId(memberId));
    excludedMemberIds.addAll(findBlockedMemberIdsUseCase.findBlockRelatedMemberIds(memberId));
    return excludedMemberIds;
  }

  private Date getExpireAt() {
    LocalDateTime expireDateTime = LocalDate.now().plusDays(1).atStartOfDay();
    return Date.from(expireDateTime.atZone(ZoneId.systemDefault()).toInstant());
  }

  private String buildKey(IntroductionCacheKeyPrefix prefix, long memberId) {
    return prefix.getPrefix() + memberId;
  }

  @FunctionalInterface
  public interface IntroductionConditionSupplier<T> {
    IntroductionSearchCondition get(
        Set<Long> excludedMemberIds, MemberIdeal memberIdeal, MemberSummary member, T criteria);
  }
}
