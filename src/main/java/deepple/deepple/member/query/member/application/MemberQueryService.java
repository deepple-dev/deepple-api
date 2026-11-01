package deepple.deepple.member.query.member.application;

import deepple.deepple.common.event.Events;
import deepple.deepple.community.command.domain.profileexchange.ProfileExchangeStatus;
import deepple.deepple.member.command.application.member.exception.MemberNotFoundException;
import deepple.deepple.member.command.domain.member.ActivityStatus;
import deepple.deepple.member.presentation.member.MemberMapper;
import deepple.deepple.member.presentation.member.dto.MemberProfileResponse;
import deepple.deepple.member.query.member.application.event.MemberProfileRetrievedEvent;
import deepple.deepple.member.query.member.application.exception.ProfileAccessDeniedException;
import deepple.deepple.member.query.member.infra.MemberQueryRepository;
import deepple.deepple.member.query.member.view.*;
import deepple.deepple.member.query.profileimage.ProfileImageQueryRepository;
import deepple.deepple.member.query.profileimage.view.ProfileImageView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {
    private final MemberQueryRepository memberQueryRepository;
    private final ProfileImageQueryRepository profileImageQueryRepository;


    public MemberInfoView getInfoCache(Long memberId) {
        return memberQueryRepository.findInfoByMemberId(memberId).orElseThrow(MemberNotFoundException::new);
    }

    public MemberProfileView getProfile(Long memberId) {
        return memberQueryRepository.findProfileByMemberId(memberId).orElseThrow(MemberNotFoundException::new);
    }

    public MemberContactView getContact(Long memberId) {
        return memberQueryRepository.findContactsByMemberId(memberId).orElseThrow(MemberNotFoundException::new);
    }

    public HeartBalanceView getHeartBalance(Long memberId) {
        return memberQueryRepository.findHeartBalanceByMemberId(memberId).orElseThrow(MemberNotFoundException::new);
    }

    public MemberProfileResponse getMemberProfile(Long memberId, Long otherMemberId) {
        ProfileAccessView profileAccessView = memberQueryRepository.findProfileAccessViewByMemberId(memberId,
            otherMemberId).orElseThrow(MemberNotFoundException::new);
        validateProfileAccessView(profileAccessView, memberId);

        OtherMemberProfileView profileView = memberQueryRepository.findOtherProfileByMemberId(memberId, otherMemberId)
            .orElseThrow(MemberNotFoundException::new);

        List<InterviewResultView> interviewResultViews = memberQueryRepository.findInterviewsByMemberId(otherMemberId);

        List<ProfileImageView> profileImages = profileImageQueryRepository.findByMemberId(otherMemberId);

        Events.raise(MemberProfileRetrievedEvent.of(memberId, otherMemberId,
            profileAccessView.matchRequesterId(), profileAccessView.matchResponderId()));

        return new MemberProfileResponse(MemberMapper.toBasicInfo(profileView.basicMemberInfo(), profileImages),
            profileView.matchInfo(),
            MemberMapper.toContactInfo(profileView.contactView(), profileView.matchInfo(), otherMemberId),
            profileView.profileExchangeInfo(),
            profileView.introductionInfo(), interviewResultViews);
    }

    private void validateProfileAccessView(ProfileAccessView profileAccessView, Long memberId) {
        if (profileAccessView.isBlocked()) { // 차단 당한 경우.
            throw new ProfileAccessDeniedException();
        }
        if (profileAccessView.activityStatus() == null ||
            !ActivityStatus.valueOf(profileAccessView.activityStatus())
                .equals(ActivityStatus.ACTIVE)) { // 상대방이 활성화 상태가 아닌 경우.
            throw new ProfileAccessDeniedException();
        }
        if (profileAccessView.isIntroduced()) { // 소개를 받은 경우.
            return;
        }
        if (memberId.equals(profileAccessView.matchResponderId())) { // 매치 응답자인 경우.
            return;
        }
        if (profileAccessView.likeReceived()) { // 좋아요를 받은 경우.
            return;
        }
        if (ProfileExchangeStatus.APPROVE.name().equals(profileAccessView.profileExchangeStatus())) { // 프로필 교환이 완료된 경우.
            return;
        }
        if (memberId.equals(profileAccessView.profileExchangeResponderId())) { // 프로필 교환 요청을 받은 경우.
            return;
        }
        throw new ProfileAccessDeniedException();
    }
}
