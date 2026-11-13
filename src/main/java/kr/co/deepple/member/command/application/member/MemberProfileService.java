package kr.co.deepple.member.command.application.member;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.member.ActivityStatus;
import kr.co.deepple.member.command.domain.member.Gender;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
import kr.co.deepple.member.command.domain.member.PrimaryContactType;
import kr.co.deepple.member.command.domain.member.event.MemberBecameActiveEvent;
import kr.co.deepple.member.command.domain.member.event.MemberBecameDormantEvent;
import kr.co.deepple.member.command.domain.member.event.MemberProfileInitializedEvent;
import kr.co.deepple.member.command.domain.member.event.MemberSummaryChangedEvent;
import kr.co.deepple.member.presentation.member.MemberMapper;
import kr.co.deepple.member.presentation.member.dto.MemberProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberProfileService {

  private final MemberCommandRepository memberCommandRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void updateMember(long memberId, MemberProfileUpdateRequest request) {
    Member member = getMemberById(memberId);
    member.updateProfile(MemberMapper.toMemberProfile(request));
    publishSummaryChangedEvent(member);
    if (member.isProfileInitialized()) {
      eventPublisher.publishEvent(MemberProfileInitializedEvent.from(memberId));
    }
  }

  @Transactional
  public void deactivate(long memberId) {
    getMemberById(memberId).deactivate();
    eventPublisher.publishEvent(MemberBecameDormantEvent.from(memberId));
  }

  @Transactional
  public void reactivate(long memberId) {
    getMemberById(memberId).reactivate();
    eventPublisher.publishEvent(MemberBecameActiveEvent.from(memberId));
  }

  @Transactional
  public void validateContactTypeSetting(long memberId, String contactType) {
    Member member = getMemberById(memberId);
    if (PrimaryContactType.PHONE_NUMBER.name().equals(contactType)
        && member.getPhoneNumber() == null) {
      throw new DeeppleException(
          MemberErrorCode.CONTACT_TYPE_SETTING_NEEDED,
          "memberId=" + memberId + ", contactType=" + contactType);
    } else if (PrimaryContactType.KAKAO.name().equals(contactType) && member.getKakaoId() == null) {
      throw new DeeppleException(
          MemberErrorCode.CONTACT_TYPE_SETTING_NEEDED,
          "memberId=" + memberId + ", contactType=" + contactType);
    }
  }

  @Transactional
  public void approveScreening(long memberId) {
    Member member = getMemberById(memberId);
    member.approveScreening();
    eventPublisher.publishEvent(MemberBecameActiveEvent.from(memberId));
    changeProfilePublishStatus(memberId, true);
  }

  @Transactional
  public void changeReject(long memberId) {
    Member member = getMemberById(memberId);
    member.changeToRejected();
    changeProfilePublishStatus(memberId, false);
  }

  @Transactional
  public void changeWaiting(long memberId) {
    Member member = getMemberById(memberId);
    member.changeToWaiting();
    changeProfilePublishStatus(memberId, false);
  }

  @Transactional
  public void changeProfilePublishStatus(long memberId, boolean publishStatus) {
    Member member = getMemberById(memberId);
    if (publishStatus) {
      publish(member);
    } else {
      nonPublish(member);
    }
  }

  @Transactional
  public void changeMemberActivityStatus(long memberId, String status) {
    Member member = getMemberById(memberId);
    ActivityStatus activityStatus = getActivityStatus(status);
    member.changeActivityStatus(activityStatus);
    if (activityStatus == ActivityStatus.DORMANT) {
      eventPublisher.publishEvent(MemberBecameDormantEvent.from(memberId));
    }
    if (activityStatus == ActivityStatus.ACTIVE) {
      eventPublisher.publishEvent(MemberBecameActiveEvent.from(memberId));
    }

    if (activityStatus != ActivityStatus.ACTIVE) {
      nonPublish(member);
    } else {
      publish(member);
    }
  }

  @Transactional
  public void markDatingExamSubmitted(long memberId) {
    Member member = getMemberById(memberId);
    member.markDatingExamSubmitted();
  }

  private void publishSummaryChangedEvent(Member member) {
    Gender gender = member.getGender();
    eventPublisher.publishEvent(
        MemberSummaryChangedEvent.of(
            member.getId(), gender != null ? gender.name() : null, member.isVip()));
  }

  private void publish(Member member) {
    member.publishProfile();
  }

  private void nonPublish(Member member) {
    member.nonPublishProfile();
  }

  private Member getMemberById(long memberId) {
    return memberCommandRepository
        .findById(memberId)
        .orElseThrow(
            () -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
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
