package kr.co.deepple.admin.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;
import org.springframework.modulith.events.Externalized;

@Externalized(AdminTopic.NAME + "::#{#this.memberId()}")
public record ScreeningApprovedEvent(long adminId, long memberId, @NonNull EventMetadata metadata)
    implements Event {

  public static ScreeningApprovedEvent of(long adminId, long memberId) {
    return new ScreeningApprovedEvent(adminId, memberId, EventMetadata.create());
  }
}
