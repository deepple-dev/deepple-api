package kr.co.deepple.auth.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record MemberAccountTokenRefreshedEvent(long accountId, @NonNull EventMetadata metadata)
    implements Event {
  public static MemberAccountTokenRefreshedEvent from(long accountId) {
    return new MemberAccountTokenRefreshedEvent(accountId, EventMetadata.create());
  }
}
