package kr.co.deepple.match.command.domain.match.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record MatchRequestCompletedEvent(
    long requesterId, long responderId, @NonNull EventMetadata metadata) implements Event {

  public static MatchRequestCompletedEvent of(long requesterId, long responderId) {
    return new MatchRequestCompletedEvent(requesterId, responderId, EventMetadata.create());
  }
}
