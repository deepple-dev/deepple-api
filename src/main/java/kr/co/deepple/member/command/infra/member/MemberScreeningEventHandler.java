package kr.co.deepple.member.command.infra.member;

import kr.co.deepple.admin.event.ScreeningApprovedEvent;
import kr.co.deepple.admin.event.ScreeningCreatedEvent;
import kr.co.deepple.admin.event.ScreeningRejectedEvent;
import kr.co.deepple.member.command.application.member.MemberProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class MemberScreeningEventHandler {

  private final MemberProfileService memberProfileService;

  @Async
  @TransactionalEventListener(
      value = ScreeningApprovedEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handle(ScreeningApprovedEvent event) {
    memberProfileService.approveScreening(event.memberId());
  }

  @Async
  @TransactionalEventListener(
      value = ScreeningRejectedEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handle(ScreeningRejectedEvent event) {
    memberProfileService.changeReject(event.memberId());
  }

  @Async
  @TransactionalEventListener(
      value = ScreeningCreatedEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handle(ScreeningCreatedEvent event) {
    memberProfileService.changeWaiting(event.memberId());
  }
}
