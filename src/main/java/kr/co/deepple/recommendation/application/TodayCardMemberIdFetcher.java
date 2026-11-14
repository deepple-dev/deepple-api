package kr.co.deepple.recommendation.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import kr.co.deepple.member.application.port.in.MemberSummary;
import kr.co.deepple.recommendation.adapter.out.persistence.IntroductionQueryRepository;
import kr.co.deepple.recommendation.adapter.out.persistence.IntroductionRedisRepository;
import kr.co.deepple.recommendation.application.dto.IntroductionSearchCondition;
import kr.co.deepple.recommendation.application.port.out.MemberIdealCommandRepository;
import kr.co.deepple.recommendation.domain.MemberIdeal;
import kr.co.deepple.recommendation.domain.RecommendationErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodayCardMemberIdFetcher {

  private static final int COMBINATION_NONE_SELECTABLE_SIZE = 2;
  private static final long MALE_LIMIT = 3L;
  private static final long FEMALE_LIMIT = 4L;
  private static final String MALE = "MALE";

  private final IntroductionQueryRepository introductionQueryRepository;
  private final IntroductionRedisRepository introductionRedisRepository;
  private final FindMemberSummaryUseCase findMemberSummaryUseCase;
  private final MemberIdealCommandRepository memberIdealCommandRepository;

  public Set<Long> fetch(long memberId, IntroductionCacheKeyPrefix cacheKeyPrefix) {
    String cacheKey = buildKey(cacheKeyPrefix, memberId);
    Set<Long> cachedIds = introductionRedisRepository.findIntroductionMemberIds(cacheKey);
    if (!cachedIds.isEmpty()) {
      return cachedIds;
    }
    MemberSummary member = findMemberSummaryUseCase.getMemberSummary(memberId);
    List<IntroductionSearchCondition> conditions = getConditions(member);
    Set<Long> introductionMemberIds = findAllIntroductionMemberId(conditions, member);
    introductionRedisRepository.saveIntroductionMemberIds(
        cacheKey, introductionMemberIds, getExpireAt());
    return introductionMemberIds;
  }

  private List<IntroductionSearchCondition> getConditions(MemberSummary member) {
    MemberIdeal memberIdeal =
        memberIdealCommandRepository
            .findByMemberId(member.memberId())
            .orElseThrow(() ->
                new DeeppleException(
                    RecommendationErrorCode.MEMBER_IDEAL_NOT_FOUND,
                    "memberId=" + member.memberId()));
    Set<Long> excludedMemberIds = findExcludedMemberIds(member.memberId());
    String oppositeGender = member.oppositeGender();
    if (!memberIdeal.isUpdated()) {
      return List.of(
          IntroductionSearchCondition.ofTodayCardDefault(
              excludedMemberIds, oppositeGender, member));
    }
    IntroductionSearchCondition baseCondition =
        IntroductionSearchCondition.ofIdeal(excludedMemberIds, memberIdeal, oppositeGender);
    return IntroductionSearchConditionCombinator.generateCombinations(
        baseCondition, COMBINATION_NONE_SELECTABLE_SIZE);
  }

  private Set<Long> findExcludedMemberIds(long memberId) {
    Set<Long> excludedMemberIds = new HashSet<>();
    excludedMemberIds.add(memberId);
    excludedMemberIds.addAll(introductionQueryRepository.findAllMatchRequestedMemberId(memberId));
    excludedMemberIds.addAll(introductionQueryRepository.findAllMatchRequestingMemberId(memberId));
    excludedMemberIds.addAll(introductionQueryRepository.findAllIntroducedMemberId(memberId));
    excludedMemberIds.addAll(introductionQueryRepository.findAllBlockedMemberId(memberId));
    excludedMemberIds.addAll(introductionQueryRepository.findAllBlockingMemberId(memberId));
    return excludedMemberIds;
  }

  private Set<Long> findAllIntroductionMemberId(
      List<IntroductionSearchCondition> conditions, MemberSummary member) {
    Set<Long> introductionMemberIds = new HashSet<>();
    long limit = getLimit(member);
    for (IntroductionSearchCondition condition : conditions) {
      long currentLimit = limit - introductionMemberIds.size();
      introductionMemberIds.addAll(
          introductionQueryRepository.findAllIntroductionMemberId(condition, currentLimit));
      if (introductionMemberIds.size() == limit) {
        break;
      }
    }
    return introductionMemberIds;
  }

  private long getLimit(MemberSummary member) {
    return MALE.equals(member.requireGender()) ? MALE_LIMIT : FEMALE_LIMIT;
  }

  private Date getExpireAt() {
    LocalDateTime expireDateTime = LocalDate.now().plusDays(1).atStartOfDay();
    return Date.from(expireDateTime.atZone(ZoneId.systemDefault()).toInstant());
  }

  private String buildKey(IntroductionCacheKeyPrefix prefix, long memberId) {
    return prefix.getPrefix() + memberId;
  }
}
