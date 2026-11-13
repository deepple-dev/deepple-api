package kr.co.deepple.member.command.application.member;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.member.Gender;
import kr.co.deepple.member.command.domain.member.Grade;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
import kr.co.deepple.member.event.MemberSettingUpdatedEvent;
import kr.co.deepple.member.event.MemberSummaryChangedEvent;
import kr.co.deepple.member.presentation.member.dto.AdminMemberSettingUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMemberService {

  private final MemberCommandRepository memberCommandRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void updateMemberSetting(long memberId, AdminMemberSettingUpdateRequest request) {
    Grade grade = Grade.from(request.grade());
    Member member = getMember(memberId);
    member.updateSetting(grade, request.isVip());
    eventPublisher.publishEvent(
        MemberSettingUpdatedEvent.of(memberId, request.isPushNotificationEnabled()));
    Gender gender = member.getGender();
    eventPublisher.publishEvent(
        MemberSummaryChangedEvent.of(
            memberId, gender != null ? gender.name() : null, member.isVip()));
    log.atInfo().addKeyValue("memberId", memberId).log("관리자 회원 설정 변경");
  }

  private Member getMember(long memberId) {
    return memberCommandRepository
        .findById(memberId)
        .orElseThrow(
            () -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
  }
}
