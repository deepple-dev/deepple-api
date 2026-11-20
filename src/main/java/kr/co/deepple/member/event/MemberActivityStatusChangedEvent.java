package kr.co.deepple.member.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;
import org.springframework.modulith.events.Externalized;

@Externalized(MemberTopic.NAME + "::#{#this.memberId()}")
public record MemberActivityStatusChangedEvent(
    long memberId, @NonNull String activityStatus, @NonNull EventMetadata metadata)
    implements Event {

  public static MemberActivityStatusChangedEvent of(long memberId, String activityStatus) {
    return new MemberActivityStatusChangedEvent(memberId, activityStatus, EventMetadata.create());
  }
}
