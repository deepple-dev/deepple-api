package kr.co.deepple.profile.application;

import java.util.Optional;
import kr.co.deepple.block.application.port.in.CheckBlockUseCase;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.community.application.port.in.FindProfileExchangeUseCase;
import kr.co.deepple.community.application.port.in.ProfileExchangeSummary;
import kr.co.deepple.datingexam.application.port.in.DominantPersonalityType;
import kr.co.deepple.datingexam.application.port.in.FindDominantPersonalityTypesUseCase;
import kr.co.deepple.like.application.port.in.FindLikeLevelUseCase;
import kr.co.deepple.member.application.port.in.FindMemberScreeningProfilesUseCase;
import kr.co.deepple.member.application.port.in.GetMemberDetailUseCase;
import kr.co.deepple.member.application.port.in.MemberDetail;
import kr.co.deepple.member.application.port.in.MemberProfileImage;
import kr.co.deepple.profile.application.dto.MemberSummaryView;
import kr.co.deepple.profile.domain.ProfileErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberSummaryQueryService {

  private static final String ACTIVE = "ACTIVE";
  private static final String SENT = "SENT";
  private static final String RECEIVED = "RECEIVED";

  private final GetMemberDetailUseCase getMemberDetailUseCase;
  private final FindMemberScreeningProfilesUseCase findMemberScreeningProfilesUseCase;
  private final FindDominantPersonalityTypesUseCase findDominantPersonalityTypesUseCase;
  private final FindLikeLevelUseCase findLikeLevelUseCase;
  private final FindProfileExchangeUseCase findProfileExchangeUseCase;
  private final CheckBlockUseCase checkBlockUseCase;

  public MemberSummaryView getMemberSummary(long memberId, long otherMemberId) {
    MemberDetail otherMember = getMemberDetailUseCase.getMemberDetail(otherMemberId);
    validateAccess(memberId, otherMember);

    Optional<DominantPersonalityType> personalityType =
        findDominantPersonalityTypesUseCase.findDominantPersonalityType(otherMemberId);
    Optional<ProfileExchangeSummary> profileExchange =
        findProfileExchangeUseCase.findProfileExchangeBetween(memberId, otherMemberId);
    return new MemberSummaryView(
        otherMemberId,
        otherMember.nickname(),
        personalityType.map(DominantPersonalityType::code).orElse(null),
        personalityType.map(DominantPersonalityType::name).orElse(null),
        findPrimaryImageUrl(otherMemberId),
        otherMember.age(),
        otherMember.gender(),
        otherMember.city(),
        otherMember.district(),
        otherMember.mbti(),
        otherMember.hobbies(),
        findLikeLevelUseCase.findLikeLevel(memberId, otherMemberId).orElse(null),
        profileExchange.map(ProfileExchangeSummary::profileExchangeId).orElse(null),
        profileExchange.map(ProfileExchangeSummary::status).orElse(null),
        profileExchange
            .map(exchange -> exchange.requesterId() == memberId ? SENT : RECEIVED)
            .orElse(null));
  }

  private void validateAccess(long memberId, MemberDetail otherMember) {
    long otherMemberId = otherMember.memberId();
    if (!ACTIVE.equals(otherMember.activityStatus())
        || checkBlockUseCase.isBlocked(memberId, otherMemberId)
        || checkBlockUseCase.isBlocked(otherMemberId, memberId)) {
      throw new DeeppleException(
          ProfileErrorCode.PROFILE_ACCESS_DENIED,
          "memberId=" + memberId + ", otherMemberId=" + otherMemberId);
    }
  }

  private String findPrimaryImageUrl(long memberId) {
    return findMemberScreeningProfilesUseCase.findProfileImages(memberId).stream()
        .filter(MemberProfileImage::primary)
        .findFirst()
        .map(MemberProfileImage::imageUrl)
        .orElse(null);
  }
}
