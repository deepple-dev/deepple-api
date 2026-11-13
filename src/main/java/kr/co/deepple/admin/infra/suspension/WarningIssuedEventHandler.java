package kr.co.deepple.admin.infra.suspension;

import kr.co.deepple.admin.command.application.suspension.SuspensionService;
import kr.co.deepple.admin.event.WarningIssuedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class WarningIssuedEventHandler {

  private final SuspensionService suspensionService;

  @Async
  @TransactionalEventListener(
      value = WarningIssuedEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handle(WarningIssuedEvent event) {
    suspensionService.suspendByWarningCount(
        event.adminId(), event.memberId(), event.warningCount());
  }
}
