package kr.co.deepple.admin.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record MemberUnsuspendedEvent(long memberId, @NonNull EventMetadata metadata)
    implements Event {

  public static MemberUnsuspendedEvent from(long memberId) {
    return new MemberUnsuspendedEvent(memberId, EventMetadata.create());
  }
}
