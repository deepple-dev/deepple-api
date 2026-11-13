package kr.co.deepple.admin.infra.warning;

import java.util.Set;
import kr.co.deepple.admin.command.application.warning.WarningService;
import kr.co.deepple.admin.presentation.warning.WarningCreateRequest;
import kr.co.deepple.report.event.ReportWarnedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportWarnedEventHandler {

  private final WarningService warningService;

  @ApplicationModuleListener
  public void handle(ReportWarnedEvent event) {
    WarningCreateRequest request =
        new WarningCreateRequest(
            event.reporteeId(), Set.of("REPORT"), true, event.idempotencyKey());
    warningService.issue(event.adminId(), request);
  }
}
