package kr.co.deepple.auth.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record MemberAccountLoggedInEvent(long accountId, @NonNull EventMetadata metadata)
    implements Event {
  public static MemberAccountLoggedInEvent from(long accountId) {
    return new MemberAccountLoggedInEvent(accountId, EventMetadata.create());
  }
}
