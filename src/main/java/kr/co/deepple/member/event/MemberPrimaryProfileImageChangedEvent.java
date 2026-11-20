package kr.co.deepple.member.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;
import org.springframework.modulith.events.Externalized;

@Externalized(MemberTopic.NAME + "::#{#this.memberId()}")
public record MemberPrimaryProfileImageChangedEvent(
    long memberId, String imageUrl, @NonNull EventMetadata metadata) implements Event {

  public static MemberPrimaryProfileImageChangedEvent of(long memberId, String imageUrl) {
    return new MemberPrimaryProfileImageChangedEvent(memberId, imageUrl, EventMetadata.create());
  }
}
