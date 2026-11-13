package kr.co.deepple.match.command.domain.match.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record MatchRequestedEvent(
    long requesterId,
    @NonNull String requesterName,
    long responderId,
    String matchType,
    String contactType,
    @NonNull EventMetadata metadata)
    implements Event {

  public static MatchRequestedEvent of(
      long requesterId,
      String requesterName,
      long responderId,
      String matchType,
      String contactType) {
    return new MatchRequestedEvent(
        requesterId, requesterName, responderId, matchType, contactType, EventMetadata.create());
  }
}
