package kr.co.deepple.auth.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;
import org.springframework.modulith.events.Externalized;

@Externalized(AuthTopic.NAME + "::#{#this.accountId()}")
public record MemberAccountDeletedEvent(long accountId, @NonNull EventMetadata metadata)
    implements Event {
  public static MemberAccountDeletedEvent from(long accountId) {
    return new MemberAccountDeletedEvent(accountId, EventMetadata.create());
  }
}
