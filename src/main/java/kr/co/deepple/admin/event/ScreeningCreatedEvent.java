package kr.co.deepple.admin.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record ScreeningCreatedEvent(long memberId, @NonNull EventMetadata metadata)
    implements Event {

  public static ScreeningCreatedEvent from(long memberId) {
    return new ScreeningCreatedEvent(memberId, EventMetadata.create());
  }
}
