package kr.co.deepple.notification.infra;

import kr.co.deepple.member.event.MemberBecameActiveEvent;
import kr.co.deepple.member.event.MemberBecameDormantEvent;
import kr.co.deepple.member.event.MemberLoggedInEvent;
import kr.co.deepple.member.event.MemberLoggedOutEvent;
import kr.co.deepple.notification.command.application.DeviceRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class DeviceRegistrationEventHandler {

  private final DeviceRegistrationService deviceRegistrationService;

  @Async
  @TransactionalEventListener(
      value = MemberLoggedInEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handleMemberLoggedInEvent(MemberLoggedInEvent event) {
    deviceRegistrationService.activateByMemberId(event.memberId());
  }

  @Async
  @TransactionalEventListener(
      value = MemberLoggedOutEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handleMemberLoggedOutEvent(MemberLoggedOutEvent event) {
    deviceRegistrationService.deactivateByMemberId(event.memberId());
  }

  @Async
  @TransactionalEventListener(
      value = MemberBecameDormantEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handleMemberBecameDormantEvent(MemberBecameDormantEvent event) {
    deviceRegistrationService.deactivateByMemberId(event.memberId());
  }

  @Async
  @TransactionalEventListener(
      value = MemberBecameActiveEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handleMemberActivatedEvent(MemberBecameActiveEvent event) {
    deviceRegistrationService.activateByMemberId(event.memberId());
  }
}
