package kr.co.deepple.member.command.infra.member;

import kr.co.deepple.admin.command.domain.suspension.MemberSuspendedEvent;
import kr.co.deepple.admin.command.domain.suspension.MemberUnsuspendedEvent;
import kr.co.deepple.member.command.application.member.MemberProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class SuspendedEventHandler {

  private final MemberProfileService memberProfileService;

  @Async
  @TransactionalEventListener(
      value = MemberSuspendedEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handle(MemberSuspendedEvent event) {
    memberProfileService.changeMemberActivityStatus(event.memberId(), event.status());
  }

  @Async
  @TransactionalEventListener(
      value = MemberUnsuspendedEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handle(MemberUnsuspendedEvent event) {
    memberProfileService.changeMemberActivityStatus(event.memberId(), "ACTIVE");
  }
}
