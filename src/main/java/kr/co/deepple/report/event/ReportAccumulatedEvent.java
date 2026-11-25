package kr.co.deepple.report.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;
import org.springframework.modulith.events.Externalized;

@Externalized(ReportTopic.NAME + "::#{#this.targetId()}")
public record ReportAccumulatedEvent(
    @NonNull String targetType, long targetId, @NonNull EventMetadata metadata) implements Event {

  public static ReportAccumulatedEvent of(String targetType, long targetId) {
    return new ReportAccumulatedEvent(targetType, targetId, EventMetadata.create());
  }
}
