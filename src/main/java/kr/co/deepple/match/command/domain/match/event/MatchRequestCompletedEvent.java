package kr.co.deepple.match.command.domain.match.event;

import kr.co.deepple.common.event.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MatchRequestCompletedEvent extends Event {

  private final Long requesterId;
  private final Long responderId;

  public static MatchRequestCompletedEvent of(Long requesterId, Long responderId) {
    return new MatchRequestCompletedEvent(requesterId, responderId);
  }
}
