package kr.co.deepple.report.adapter.in.event;

import kr.co.deepple.report.application.ReportAccumulationService;
import kr.co.deepple.report.domain.ContentReportedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportAccumulationEventHandler {

  private final ReportAccumulationService reportAccumulationService;

  @ApplicationModuleListener
  public void handle(ContentReportedEvent event) {
    reportAccumulationService.checkAccumulation(event.targetType(), event.targetId());
  }
}
