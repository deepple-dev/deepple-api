package kr.co.deepple.member.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;
import org.springframework.modulith.events.Externalized;

@Externalized(MemberTopic.NAME + "::#{#this.memberId()}")
public record MemberSettingUpdatedEvent(
    long memberId, boolean isPushNotificationEnabled, @NonNull EventMetadata metadata)
    implements Event {

  public static MemberSettingUpdatedEvent of(long memberId, boolean isPushNotificationEnabled) {
    return new MemberSettingUpdatedEvent(
        memberId, isPushNotificationEnabled, EventMetadata.create());
  }
}
