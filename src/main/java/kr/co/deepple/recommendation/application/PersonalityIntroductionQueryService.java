package kr.co.deepple.recommendation.application;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.datingexam.application.port.in.PersonalityTypeMemberFinder;
import kr.co.deepple.datingexam.application.port.in.ValidatePersonalityTypeUseCase;
import kr.co.deepple.recommendation.adapter.out.persistence.IntroductionQueryRepository;
import kr.co.deepple.recommendation.adapter.out.persistence.PersonalityIntroductionRedisRepository;
import kr.co.deepple.recommendation.application.dto.MemberIntroductionProfileView;
import kr.co.deepple.recommendation.application.dto.PersonalityIntroductionOpenState;
import kr.co.deepple.recommendation.application.dto.PersonalityIntroductionOpenStatusView;
import kr.co.deepple.recommendation.domain.RecommendationErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonalityIntroductionQueryService {

  private static final int MAX_CANDIDATES = 3;

  private final PersonalityTypeMemberFinder personalityTypeMemberFinder;
  private final ValidatePersonalityTypeUseCase validatePersonalityTypeUseCase;
  private final IntroductionQueryService introductionQueryService;
  private final IntroductionQueryRepository introductionQueryRepository;
  private final PersonalityIntroductionRedisRepository personalityIntroductionRedisRepository;

  /**
   * 유형별 이상형을 오픈하고 목록(최대 3명)을 조회한다. 같은 유형이 이미 열려 있으면 24시간 고정된 목록을 그대로 반환하고, 다른 유형이 열려 있으면 예외를 던진다.
   * 후보가 0명이면 오픈을 소진하지 않는다.
   */
  public List<MemberIntroductionProfileView> open(long memberId, String type) {
    validatePersonalityTypeUseCase.validate(type);
    Optional<PersonalityIntroductionOpenState> openState =
        personalityIntroductionRedisRepository.findOpenState(memberId);
    if (openState.isPresent()) {
      PersonalityIntroductionOpenState state = openState.get();
      if (!state.type().equals(type)) {
        throw new DeeppleException(
            RecommendationErrorCode.PERSONALITY_INTRODUCTION_ALREADY_OPENED,
            "openedType=" + state.type() + ", requestedType=" + type);
      }
      return toProfileViews(memberId, state.memberIds());
    }

    List<Long> candidateMemberIds = computeCandidateMemberIds(memberId, type);
    if (candidateMemberIds.isEmpty()) {
      return List.of();
    }
    personalityIntroductionRedisRepository.saveOpenState(
        memberId,
        new PersonalityIntroductionOpenState(type, candidateMemberIds, LocalDateTime.now()));
    return toProfileViews(memberId, candidateMemberIds);
  }

  /** 현재 오픈되어 있는 유형과 오픈 시각을 조회한다. 오픈 이력이 없으면 유형과 오픈 시각이 비어 있다. */
  public PersonalityIntroductionOpenStatusView getOpenStatus(long memberId) {
    return personalityIntroductionRedisRepository
        .findOpenState(memberId)
        .map(state -> new PersonalityIntroductionOpenStatusView(state.type(), state.openedAt()))
        .orElseGet(() -> new PersonalityIntroductionOpenStatusView(null, null));
  }

  private List<Long> computeCandidateMemberIds(long memberId, String type) {
    List<Long> typeMemberIds =
        personalityTypeMemberFinder.findMemberIdsByDominantType(memberId, type);
    Set<Long> excludedMemberIds = findExcludedMemberIds(memberId);
    return typeMemberIds.stream()
        .filter(id -> !excludedMemberIds.contains(id))
        .limit(MAX_CANDIDATES)
        .toList();
  }

  private Set<Long> findExcludedMemberIds(long memberId) {
    Set<Long> excludedMemberIds = new HashSet<>();
    excludedMemberIds.addAll(introductionQueryRepository.findAllMatchRequestedMemberId(memberId));
    excludedMemberIds.addAll(introductionQueryRepository.findAllMatchRequestingMemberId(memberId));
    excludedMemberIds.addAll(introductionQueryRepository.findAllIntroducedMemberId(memberId));
    return excludedMemberIds;
  }

  private List<MemberIntroductionProfileView> toProfileViews(long memberId, List<Long> memberIds) {
    return introductionQueryService.findMemberIntroductionProfileViews(
        memberId, new LinkedHashSet<>(memberIds));
  }
}
