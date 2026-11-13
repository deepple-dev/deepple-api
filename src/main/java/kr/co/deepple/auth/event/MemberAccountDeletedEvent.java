package kr.co.deepple.auth.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record MemberAccountDeletedEvent(long accountId, @NonNull EventMetadata metadata)
    implements Event {
  public static MemberAccountDeletedEvent from(long accountId) {
    return new MemberAccountDeletedEvent(accountId, EventMetadata.create());
  }
}
