package kr.co.deepple.auth.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;
import org.springframework.modulith.events.Externalized;

@Externalized(AuthTopic.NAME + "::#{#this.accountId()}")
public record MemberAccountTokenRefreshedEvent(long accountId, @NonNull EventMetadata metadata)
    implements Event {
  public static MemberAccountTokenRefreshedEvent from(long accountId) {
    return new MemberAccountTokenRefreshedEvent(accountId, EventMetadata.create());
  }
}
