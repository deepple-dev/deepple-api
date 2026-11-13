package kr.co.deepple.report.command.domain.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record ReportWarnedEvent(
    long adminId, long reporteeId, String reportReason, @NonNull EventMetadata metadata)
    implements Event {

  public static ReportWarnedEvent of(long adminId, long reporteeId, String reportReason) {
    return new ReportWarnedEvent(adminId, reporteeId, reportReason, EventMetadata.create());
  }
}
