package kr.co.deepple.recommendation.application;

import java.util.Set;
import kr.co.deepple.block.application.port.in.CheckBlockUseCase;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.datingexam.application.port.in.ValidatePersonalityTypeUseCase;
import kr.co.deepple.heart.application.port.in.UseHeartCommand;
import kr.co.deepple.heart.application.port.in.UseHeartUseCase;
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import kr.co.deepple.member.application.port.in.MemberSummary;
import kr.co.deepple.recommendation.adapter.out.persistence.PersonalityIntroductionRedisRepository;
import kr.co.deepple.recommendation.application.dto.PersonalityIntroductionOpenState;
import kr.co.deepple.recommendation.application.port.out.MemberIntroductionCommandRepository;
import kr.co.deepple.recommendation.domain.IntroductionType;
import kr.co.deepple.recommendation.domain.MemberIntroduction;
import kr.co.deepple.recommendation.domain.RecommendationErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonalityIntroductionUnlockService {

  private static final String HEART_USAGE_TYPE = "INTRODUCTION";

  private final FindMemberSummaryUseCase findMemberSummaryUseCase;
  private final ValidatePersonalityTypeUseCase validatePersonalityTypeUseCase;
  private final MemberIntroductionCommandRepository memberIntroductionCommandRepository;
  private final CheckBlockUseCase checkBlockUseCase;
  private final UseHeartUseCase useHeartUseCase;
  private final PersonalityIntroductionRedisRepository personalityIntroductionRedisRepository;

  /** 유형별 이상형 프로필 상세를 언락한다. 오픈된 유형의 첫 언락은 무료, 이후 언락은 하트를 차감한다. 이미 언락된 대상은 멱등하게 처리한다. */
  @Transactional
  public void unlock(long memberId, String type, long targetMemberId) {
    validatePersonalityTypeUseCase.validate(type);
    PersonalityIntroductionOpenState openState =
        personalityIntroductionRedisRepository
            .findOpenState(memberId)
            .orElseThrow(() ->
                new DeeppleException(
                    RecommendationErrorCode.INVALID_PERSONALITY_INTRODUCTION_UNLOCK,
                    "오픈된 유형별 이상형 목록이 없습니다."));
    if (!openState.type().equals(type) || !openState.memberIds().contains(targetMemberId)) {
      throw new DeeppleException(
          RecommendationErrorCode.INVALID_PERSONALITY_INTRODUCTION_UNLOCK,
          "오픈된 목록에 없는 대상입니다. targetMemberId: " + targetMemberId);
    }

    Set<Long> unlockedMemberIds =
        personalityIntroductionRedisRepository.findUnlockedMemberIds(memberId);
    if (unlockedMemberIds.contains(targetMemberId)
        || memberIntroductionCommandRepository.existsByMemberIdAndIntroducedMemberId(
            memberId, targetMemberId)) {
      return;
    }

    validateTarget(memberId, targetMemberId);

    boolean isFirstUnlock = unlockedMemberIds.isEmpty();
    MemberIntroduction memberIntroduction =
        MemberIntroduction.of(memberId, targetMemberId, IntroductionType.PERSONALITY);
    memberIntroductionCommandRepository.save(memberIntroduction);
    if (!isFirstUnlock) {
      useHeartUseCase.useHeart(
          new UseHeartCommand(
              memberId,
              HEART_USAGE_TYPE,
              IntroductionType.PERSONALITY.name(),
              IntroductionType.PERSONALITY.getDescription(),
              String.valueOf(memberIntroduction.getId())));
    }

    personalityIntroductionRedisRepository.addUnlockedMemberId(memberId, targetMemberId);
  }

  private void validateTarget(long memberId, long targetMemberId) {
    MemberSummary target = findMemberSummaryUseCase.getMemberSummary(targetMemberId);
    if (!target.isActive()) {
      throw new DeeppleException(
          RecommendationErrorCode.INTRODUCED_MEMBER_NOT_ACTIVE,
          "introducedMemberId=" + targetMemberId);
    }
    if (checkBlockUseCase.isBlocked(memberId, targetMemberId)
        || checkBlockUseCase.isBlocked(targetMemberId, memberId)) {
      throw new DeeppleException(
          RecommendationErrorCode.INTRODUCED_MEMBER_BLOCKED,
          "memberId=" + memberId + ", introducedMemberId=" + targetMemberId);
    }
  }
}
