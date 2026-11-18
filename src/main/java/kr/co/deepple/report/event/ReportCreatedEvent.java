package kr.co.deepple.report.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;
import org.springframework.modulith.events.Externalized;

@Externalized(ReportTopic.NAME + "::#{#this.reporteeId()}")
public record ReportCreatedEvent(long reporterId, long reporteeId, @NonNull EventMetadata metadata)
    implements Event {

  public static ReportCreatedEvent of(long reporterId, long reporteeId) {
    return new ReportCreatedEvent(reporterId, reporteeId, EventMetadata.create());
  }
}
