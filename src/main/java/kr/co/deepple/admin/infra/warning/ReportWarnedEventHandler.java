package kr.co.deepple.admin.infra.warning;

import java.util.Set;
import kr.co.deepple.admin.command.application.warning.WarningService;
import kr.co.deepple.admin.presentation.warning.WarningCreateRequest;
import kr.co.deepple.report.command.domain.event.ReportWarnedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class ReportWarnedEventHandler {

  private final WarningService warningService;

  @Async
  @TransactionalEventListener(
      value = ReportWarnedEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handle(ReportWarnedEvent event) {
    WarningCreateRequest request =
        new WarningCreateRequest(event.getReporteeId(), Set.of("REPORT"), true);
    warningService.issue(event.getAdminId(), request);
  }
}
