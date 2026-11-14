package kr.co.deepple.member.application;

import java.util.List;
import java.util.Objects;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.community.domain.ProfileExchangeStatus;
import kr.co.deepple.member.adapter.out.persistence.MemberQueryRepository;
import kr.co.deepple.member.adapter.out.persistence.ProfileImageQueryRepository;
import kr.co.deepple.member.application.dto.InterviewResultView;
import kr.co.deepple.member.application.dto.MemberContactView;
import kr.co.deepple.member.application.dto.MemberInfoView;
import kr.co.deepple.member.application.dto.MemberProfileResponse;
import kr.co.deepple.member.application.dto.MemberProfileView;
import kr.co.deepple.member.application.dto.OtherMemberProfileView;
import kr.co.deepple.member.application.dto.ProfileAccessView;
import kr.co.deepple.member.application.dto.ProfileImageView;
import kr.co.deepple.member.domain.ActivityStatus;
import kr.co.deepple.member.domain.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

  private final MemberQueryRepository memberQueryRepository;
  private final ProfileImageQueryRepository profileImageQueryRepository;

  public MemberInfoView getInfoCache(long memberId) {
    return memberQueryRepository
        .findInfoByMemberId(memberId)
        .orElseThrow(
            () -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
  }

  public MemberProfileView getProfile(long memberId) {
    return memberQueryRepository
        .findProfileByMemberId(memberId)
        .orElseThrow(
            () -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
  }

  public MemberContactView getContact(long memberId) {
    return memberQueryRepository
        .findContactsByMemberId(memberId)
        .orElseThrow(
            () -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
  }

  public MemberProfileResponse getMemberProfile(long memberId, long otherMemberId) {
    ProfileAccessView profileAccessView =
        memberQueryRepository
            .findProfileAccessViewByMemberId(memberId, otherMemberId)
            .orElseThrow(() ->
                new DeeppleException(
                    MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + otherMemberId));
    validateProfileAccessView(profileAccessView, memberId, otherMemberId);

    OtherMemberProfileView profileView =
        memberQueryRepository
            .findOtherProfileByMemberId(memberId, otherMemberId)
            .orElseThrow(() ->
                new DeeppleException(
                    MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + otherMemberId));

    List<InterviewResultView> interviewResultViews =
        memberQueryRepository.findInterviewsByMemberId(otherMemberId);

    List<ProfileImageView> profileImages =
        profileImageQueryRepository.findByMemberId(otherMemberId);

    return new MemberProfileResponse(
        MemberMapper.toBasicInfo(profileView.basicMemberInfo(), profileImages),
        profileView.matchInfo(),
        MemberMapper.toContactInfo(
            profileView.contactView(), profileView.matchInfo(), otherMemberId),
        profileView.profileExchangeInfo(),
        profileView.introductionInfo(),
        interviewResultViews);
  }

  private void validateProfileAccessView(
      ProfileAccessView profileAccessView, long memberId, long otherMemberId) {
    if (profileAccessView.isBlocked()) { // 차단 당한 경우.
      throw new DeeppleException(
          MemberErrorCode.PROFILE_ACCESS_DENIED,
          "memberId=" + memberId + ", otherMemberId=" + otherMemberId);
    }
    if (profileAccessView.activityStatus() == null
        || !ActivityStatus.valueOf(profileAccessView.activityStatus())
            .equals(ActivityStatus.ACTIVE)) { // 상대방이 활성화 상태가 아닌 경우.
      throw new DeeppleException(
          MemberErrorCode.PROFILE_ACCESS_DENIED,
          "memberId=" + memberId + ", otherMemberId=" + otherMemberId);
    }
    if (profileAccessView.isIntroduced()) { // 소개를 받은 경우.
      return;
    }
    if (Objects.equals(memberId, profileAccessView.matchResponderId())) { // 매치 응답자인 경우.
      return;
    }
    if (profileAccessView.likeReceived()) { // 좋아요를 받은 경우.
      return;
    }
    if (ProfileExchangeStatus.APPROVE
        .name()
        .equals(profileAccessView.profileExchangeStatus())) { // 프로필 교환이 완료된 경우.
      return;
    }
    if (Objects.equals(
        memberId, profileAccessView.profileExchangeResponderId())) { // 프로필 교환 요청을 받은 경우.
      return;
    }
    throw new DeeppleException(
        MemberErrorCode.PROFILE_ACCESS_DENIED,
        "memberId=" + memberId + ", otherMemberId=" + otherMemberId);
  }
}
