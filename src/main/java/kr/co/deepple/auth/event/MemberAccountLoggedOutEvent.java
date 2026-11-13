package kr.co.deepple.auth.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record MemberAccountLoggedOutEvent(long accountId, @NonNull EventMetadata metadata)
    implements Event {
  public static MemberAccountLoggedOutEvent from(long accountId) {
    return new MemberAccountLoggedOutEvent(accountId, EventMetadata.create());
  }
}
