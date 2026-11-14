package kr.co.deepple.recommendation.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.application.port.in.ProfileOptions;
import kr.co.deepple.member.application.port.in.ValidateProfileOptionsUseCase;
import kr.co.deepple.recommendation.application.dto.MemberIdealUpdateRequest;
import kr.co.deepple.recommendation.application.dto.MemberIdealView;
import kr.co.deepple.recommendation.application.port.out.MemberIdealQueryRepository;
import kr.co.deepple.recommendation.application.port.out.MemberIdealRepository;
import kr.co.deepple.recommendation.domain.AgeRange;
import kr.co.deepple.recommendation.domain.MemberIdeal;
import kr.co.deepple.recommendation.domain.RecommendationErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberIdealService {

  private final MemberIdealRepository memberIdealRepository;
  private final MemberIdealQueryRepository memberIdealQueryRepository;
  private final ValidateProfileOptionsUseCase validateProfileOptionsUseCase;

  @Transactional
  public void initIfAbsent(long memberId) {
    if (memberIdealRepository.existsByMemberId(memberId)) {
      return;
    }
    MemberIdeal memberIdeal = MemberIdeal.init(memberId);
    memberIdealRepository.save(memberIdeal);
  }

  @Transactional(readOnly = true)
  public MemberIdealView getMemberIdeal(long memberId) {
    return memberIdealQueryRepository
        .findMemberIdealByMemberId(memberId)
        .orElseThrow(() ->
            new DeeppleException(
                RecommendationErrorCode.MEMBER_IDEAL_NOT_FOUND, "memberId=" + memberId));
  }

  @Transactional
  public void update(MemberIdealUpdateRequest request, long memberId) {
    MemberIdeal memberIdeal = getMemberIdealByMemberId(memberId);
    AgeRange ageRange = AgeRange.of(request.minAge(), request.maxAge());
    ProfileOptions options =
        validateProfileOptionsUseCase.validate(
            new ProfileOptions(
                request.cities(),
                request.hobbies(),
                request.religion(),
                request.smokingStatus(),
                request.drinkingStatus()));
    memberIdeal.update(
        ageRange,
        options.hobbies(),
        options.cities(),
        options.religion(),
        options.smokingStatus(),
        options.drinkingStatus());
  }

  private MemberIdeal getMemberIdealByMemberId(long memberId) {
    return memberIdealRepository
        .findByMemberId(memberId)
        .orElseThrow(() ->
            new DeeppleException(
                RecommendationErrorCode.MEMBER_IDEAL_NOT_FOUND, "memberId=" + memberId));
  }
}
