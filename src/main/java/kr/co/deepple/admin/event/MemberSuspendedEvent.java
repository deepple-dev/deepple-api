package kr.co.deepple.admin.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record MemberSuspendedEvent(
    long memberId, @NonNull String status, @NonNull EventMetadata metadata) implements Event {

  public static MemberSuspendedEvent of(long memberId, String status) {
    return new MemberSuspendedEvent(memberId, status, EventMetadata.create());
  }
}
