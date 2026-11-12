package kr.co.deepple.match.command.domain.match.event;

import kr.co.deepple.common.event.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MatchRequestCompletedEvent extends Event {

  private final long requesterId;
  private final long responderId;

  public static MatchRequestCompletedEvent of(long requesterId, long responderId) {
    return new MatchRequestCompletedEvent(requesterId, responderId);
  }
}
