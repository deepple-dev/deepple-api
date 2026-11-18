package kr.co.deepple.member.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;
import org.springframework.modulith.events.Externalized;

@Externalized(MemberTopic.NAME + "::#{#this.memberId()}")
public record MemberRegisteredEvent(long memberId, @NonNull EventMetadata metadata)
    implements Event {

  public static MemberRegisteredEvent from(long memberId) {
    return new MemberRegisteredEvent(memberId, EventMetadata.create());
  }
}
