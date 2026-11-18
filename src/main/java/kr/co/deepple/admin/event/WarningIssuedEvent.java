package kr.co.deepple.admin.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;
import org.springframework.modulith.events.Externalized;

@Externalized(AdminTopic.NAME + "::#{#this.memberId()}")
public record WarningIssuedEvent(
    long adminId,
    long memberId,
    long warningCount,
    @NonNull String reasonType,
    boolean isCritical,
    @NonNull EventMetadata metadata)
    implements Event {

  public static WarningIssuedEvent of(
      long adminId, long memberId, long warningCount, String reasonType, boolean isCritical) {
    return new WarningIssuedEvent(
        adminId, memberId, warningCount, reasonType, isCritical, EventMetadata.create());
  }
}
