package kr.co.deepple.admin.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;
import org.springframework.modulith.events.Externalized;

@Externalized(AdminTopic.NAME + "::#{#this.memberId()}")
public record ScreeningRejectedEvent(
    long adminId, long memberId, String rejectionReason, @NonNull EventMetadata metadata)
    implements Event {

  public static ScreeningRejectedEvent of(long adminId, long memberId, String rejectionReason) {
    return new ScreeningRejectedEvent(adminId, memberId, rejectionReason, EventMetadata.create());
  }
}
