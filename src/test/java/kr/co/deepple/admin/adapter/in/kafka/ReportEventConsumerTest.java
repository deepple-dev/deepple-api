package kr.co.deepple.admin.adapter.in.kafka;

import static org.mockito.Mockito.verify;

import java.util.Set;
import kr.co.deepple.admin.application.WarningService;
import kr.co.deepple.admin.application.dto.WarningCreateRequest;
import kr.co.deepple.report.event.ReportWarnedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportEventConsumerTest {

  @Mock private WarningService warningService;

  @InjectMocks private ReportEventConsumer reportEventConsumer;

  @Test
  @DisplayName("신고 경고 이벤트: 신고 사유 경고를 이벤트의 멱등 키로 발급")
  void issueReportWarningWithIdempotencyKey() {
    // given
    ReportWarnedEvent event = ReportWarnedEvent.of(1L, 2L, "SPAM", "report-warning-1");

    // when
    reportEventConsumer.handle(event);

    // then
    verify(warningService)
        .issue(1L, new WarningCreateRequest(2L, Set.of("REPORT"), true, "report-warning-1"));
  }
}
