package kr.co.deepple.recommendation.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
import kr.co.deepple.recommendation.adapter.out.persistence.IntroductionQueryRepository;
import kr.co.deepple.recommendation.adapter.out.persistence.IntroductionRedisRepository;
import kr.co.deepple.recommendation.application.dto.IntroductionSearchCondition;
import kr.co.deepple.recommendation.application.port.out.MemberIdealCommandRepository;
import kr.co.deepple.recommendation.domain.MemberIdeal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IntroductionMemberIdFetcher {

  private static final int FETCH_LIMIT = 10;

  private final IntroductionQueryRepository introductionQueryRepository;
  private final IntroductionRedisRepository introductionRedisRepository;
  private final MemberCommandRepository memberCommandRepository;
  private final MemberIdealCommandRepository memberIdealCommandRepository;

  public <T> Set<Long> fetch(
      long memberId,
      IntroductionCacheKeyPrefix cacheKeyPrefix,
      T criteria,
      IntroductionConditionSupplier<T> supplier) {
    String cacheKey = buildKey(cacheKeyPrefix, memberId);
    Set<Long> cachedIds = introductionRedisRepository.findIntroductionMemberIds(cacheKey);
    if (!cachedIds.isEmpty()) {
      return cachedIds;
    }
    Set<Long> excludedMemberIds = findExcludedMemberIds(memberId);
    MemberIdeal memberIdeal =
        memberIdealCommandRepository
            .findByMemberId(memberId)
            .orElseThrow(() ->
                new DeeppleException(
                    MemberErrorCode.MEMBER_IDEAL_NOT_FOUND, "memberId=" + memberId));
    Member member =
        memberCommandRepository
            .findById(memberId)
            .orElseThrow(() ->
                new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
    IntroductionSearchCondition condition =
        supplier.get(excludedMemberIds, memberIdeal, member, criteria);
    Set<Long> introductionMemberIds =
        introductionQueryRepository.findAllIntroductionMemberId(condition, FETCH_LIMIT);
    introductionRedisRepository.saveIntroductionMemberIds(
        cacheKey, introductionMemberIds, getExpireAt());
    return introductionMemberIds;
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
        Set<Long> excludedMemberIds, MemberIdeal memberIdeal, Member member, T criteria);
  }
}
