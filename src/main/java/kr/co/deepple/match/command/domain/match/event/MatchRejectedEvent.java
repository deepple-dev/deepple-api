package kr.co.deepple.match.command.domain.match.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record MatchRejectedEvent(
    long requesterId, long responderId, String responderName, @NonNull EventMetadata metadata)
    implements Event {

  public static MatchRejectedEvent of(long requesterId, long responderId, String responderName) {
    return new MatchRejectedEvent(requesterId, responderId, responderName, EventMetadata.create());
  }
}
