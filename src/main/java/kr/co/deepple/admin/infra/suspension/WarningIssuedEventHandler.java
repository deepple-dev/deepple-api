package kr.co.deepple.admin.infra.suspension;

import kr.co.deepple.admin.command.application.suspension.SuspensionService;
import kr.co.deepple.admin.event.WarningIssuedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WarningIssuedEventHandler {

  private final SuspensionService suspensionService;

  @ApplicationModuleListener
  public void handle(WarningIssuedEvent event) {
    suspensionService.suspendByWarningCount(
        event.adminId(), event.memberId(), event.warningCount());
  }
}
