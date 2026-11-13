package kr.co.deepple.admin.command.domain.warning;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

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
