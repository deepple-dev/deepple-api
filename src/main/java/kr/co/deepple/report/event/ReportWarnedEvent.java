package kr.co.deepple.report.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record ReportWarnedEvent(
    long adminId,
    long reporteeId,
    String reportReason,
    String idempotencyKey,
    @NonNull EventMetadata metadata)
    implements Event {

  public static ReportWarnedEvent of(
      long adminId, long reporteeId, String reportReason, String idempotencyKey) {
    return new ReportWarnedEvent(
        adminId, reporteeId, reportReason, idempotencyKey, EventMetadata.create());
  }
}
