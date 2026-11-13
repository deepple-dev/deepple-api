package kr.co.deepple.match.command.domain.match.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record MatchAcceptedEvent(
    long requesterId, long responderId, String responderName, @NonNull EventMetadata metadata)
    implements Event {

  public static MatchAcceptedEvent of(long requesterId, long responderId, String responderName) {
    return new MatchAcceptedEvent(requesterId, responderId, responderName, EventMetadata.create());
  }
}
