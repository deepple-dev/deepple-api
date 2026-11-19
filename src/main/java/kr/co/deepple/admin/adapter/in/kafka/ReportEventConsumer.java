package kr.co.deepple.admin.adapter.in.kafka;

import java.util.Set;
import kr.co.deepple.admin.application.WarningService;
import kr.co.deepple.admin.application.dto.WarningCreateRequest;
import kr.co.deepple.common.event.EventConsumer;
import kr.co.deepple.report.event.ReportTopic;
import kr.co.deepple.report.event.ReportWarnedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("adminReportEventConsumer")
@RequiredArgsConstructor
@KafkaListener(topics = ReportTopic.NAME, groupId = AdminConsumerGroup.ID)
public class ReportEventConsumer extends EventConsumer {

  private final WarningService warningService;

  @KafkaHandler
  public void handle(ReportWarnedEvent event) {
    WarningCreateRequest request =
        new WarningCreateRequest(
            event.reporteeId(), Set.of("REPORT"), true, event.idempotencyKey());
    warningService.issue(event.adminId(), request);
  }
}
