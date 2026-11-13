package kr.co.deepple.match.command.domain.match.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record MatchRespondedEvent(
    long requesterId,
    long responderId,
    String matchStatus,
    String contactType,
    @NonNull EventMetadata metadata)
    implements Event {

  public static MatchRespondedEvent of(
      long requesterId, long responderId, String matchStatus, String contactType) {
    return new MatchRespondedEvent(
        requesterId, responderId, matchStatus, contactType, EventMetadata.create());
  }
}
