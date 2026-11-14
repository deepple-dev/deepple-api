package kr.co.deepple.member.application;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import kr.co.deepple.member.application.port.out.MemberRepository;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.event.MemberLoggedInEvent;
import kr.co.deepple.member.event.MemberRegisteredEvent;
import kr.co.deepple.member.event.MemberSummaryChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAccountSyncService {

  private final MemberRepository memberRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional(propagation = Propagation.MANDATORY)
  public void register(long memberId, String phoneNumber) {
    Member member = memberRepository.saveAndFlush(Member.register(memberId, phoneNumber));
    eventPublisher.publishEvent(MemberRegisteredEvent.from(member.getId()));
    eventPublisher.publishEvent(MemberSummaryChangedEvent.of(member.getId(), null, member.isVip()));
  }

  @Transactional
  public void recordLogin(long memberId, Instant loggedInAt) {
    Member member = memberRepository.findById(memberId).orElse(null);
    if (member == null) {
      log.atWarn().addKeyValue("memberId", memberId).log("회원이 없어 로그인 기록을 남기지 못했습니다.");
      return;
    }
    if (!member.isDormant()) {
      eventPublisher.publishEvent(MemberLoggedInEvent.from(memberId));
    }
    recordAccess(memberId, loggedInAt);
  }

  @Transactional
  public void recordAccess(long memberId, Instant accessedAt) {
    memberRepository.updateLastAccessedAt(
        memberId, LocalDateTime.ofInstant(accessedAt, ZoneId.systemDefault()));
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void delete(long memberId) {
    memberRepository.findByIdForUpdate(memberId).ifPresent(Member::delete);
  }
}
