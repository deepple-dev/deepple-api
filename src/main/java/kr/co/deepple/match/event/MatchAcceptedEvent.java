package kr.co.deepple.match.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;
import org.springframework.modulith.events.Externalized;

@Externalized(MatchTopic.NAME + "::#{#this.matchId()}")
public record MatchAcceptedEvent(
    long matchId,
    long requesterId,
    long responderId,
    String responderName,
    @NonNull EventMetadata metadata)
    implements Event {

  public static MatchAcceptedEvent of(
      long matchId, long requesterId, long responderId, String responderName) {
    return new MatchAcceptedEvent(
        matchId, requesterId, responderId, responderName, EventMetadata.create());
  }
}
