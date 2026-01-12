package deepple.deepple.member.command.application.member;


import deepple.deepple.member.command.application.member.exception.MemberNotFoundException;
import deepple.deepple.member.command.application.member.exception.PermanentlySuspendedMemberException;
import deepple.deepple.member.command.application.member.exception.ContactTypeSettingNeededException;
import deepple.deepple.member.command.domain.member.ActivityStatus;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import deepple.deepple.member.command.domain.member.PrimaryContactType;
import deepple.deepple.member.presentation.member.MemberMapper;
import deepple.deepple.member.presentation.member.dto.MemberProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberProfileService {

    private final MemberCommandRepository memberCommandRepository;

    @Transactional
    public void updateMember(Long memberId, MemberProfileUpdateRequest request) {
        Member member = getMemberById(memberId);
        member.updateProfile(MemberMapper.toMemberProfile(request));
    }

    @Transactional
    public void changeToDormant(Long memberId) {
        getMemberById(memberId).changeToDormant();
    }

    @Transactional
    public void changeToActive(String phoneNumber) {
        Member member = getMemberByPhoneNumber(phoneNumber);
        validateMemberStatusForActive(member);
        member.changeToActive();
    }

    private void validateMemberStatusForActive(final Member member) {
        if (member.isPermanentlySuspended()) {
            throw new PermanentlySuspendedMemberException();
        }
    }

    @Transactional
    public void validateContactTypeSetting(Long memberId, String contactType) {
        Member member = getMemberById(memberId);
        if (PrimaryContactType.PHONE_NUMBER.name().equals(contactType) && member.getPhoneNumber() == null) {
            throw new ContactTypeSettingNeededException();
        } else if (PrimaryContactType.KAKAO.name().equals(contactType) && member.getKakaoId() == null) {
            throw new ContactTypeSettingNeededException();
        }
    }

    @Transactional
    public void changeActive(Long memberId) {
        Member member = getMemberById(memberId);
        member.changeToActive();
        changeProfilePublishStatus(memberId, true);
    }

    @Transactional
    public void changeReject(Long memberId) {
        Member member = getMemberById(memberId);
        member.changeToRejected();
        changeProfilePublishStatus(memberId, false);
    }

    @Transactional
    public void changeWaiting(Long memberId) {
        Member member = getMemberById(memberId);
        member.changeToWaiting();
        changeProfilePublishStatus(memberId, false);
    }

    @Transactional
    public void changeProfilePublishStatus(Long memberId, boolean publishStatus) {
        Member member = getMemberById(memberId);
        if (publishStatus) {
            publish(member);
        } else {
            nonPublish(member);
        }
    }

    @Transactional
    public void changeMemberActivityStatus(Long memberId, String status) {
        Member member = getMemberById(memberId);
        ActivityStatus activityStatus = getActivityStatus(status);
        member.changeActivityStatus(activityStatus);

        if (activityStatus != ActivityStatus.ACTIVE) {
            nonPublish(member);
        } else {
            publish(member);
        }
    }

    @Transactional
    public void markDatingExamSubmitted(Long memberId) {
        Member member = getMemberById(memberId);
        member.markDatingExamSubmitted();
    }

    private void publish(Member member) {
        member.publishProfile();
    }

    private void nonPublish(Member member) {
        member.nonPublishProfile();
    }

    private Member getMemberById(Long memberId) {
        return memberCommandRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
    }

    private Member getMemberByPhoneNumber(String phoneNumber) {
        return memberCommandRepository.findByPhoneNumber(phoneNumber).orElseThrow(MemberNotFoundException::new);
    }

    private ActivityStatus getActivityStatus(String status) {
        if ("TEMPORARY".equals(status)) {
            return ActivityStatus.SUSPENDED_TEMPORARILY;
        } else if ("ACTIVE".equals(status)) {
            return ActivityStatus.ACTIVE;
        } else {
            return ActivityStatus.SUSPENDED_PERMANENTLY;
        }
    }
}
