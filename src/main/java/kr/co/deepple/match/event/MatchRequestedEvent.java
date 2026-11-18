package kr.co.deepple.match.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;
import org.springframework.modulith.events.Externalized;

@Externalized(MatchTopic.NAME + "::#{#this.matchId()}")
public record MatchRequestedEvent(
    long matchId,
    long requesterId,
    @NonNull String requesterName,
    long responderId,
    String matchType,
    String contactType,
    @NonNull EventMetadata metadata)
    implements Event {

  public static MatchRequestedEvent of(
      long matchId,
      long requesterId,
      String requesterName,
      long responderId,
      String matchType,
      String contactType) {
    return new MatchRequestedEvent(
        matchId,
        requesterId,
        requesterName,
        responderId,
        matchType,
        contactType,
        EventMetadata.create());
  }
}
