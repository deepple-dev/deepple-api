package kr.co.deepple.datingexam.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record AllRequiredSubjectSubmittedEvent(long memberId, @NonNull EventMetadata metadata)
    implements Event {

  public static AllRequiredSubjectSubmittedEvent from(long memberId) {
    return new AllRequiredSubjectSubmittedEvent(memberId, EventMetadata.create());
  }
}
