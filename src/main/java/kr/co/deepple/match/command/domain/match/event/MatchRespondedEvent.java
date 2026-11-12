package kr.co.deepple.match.command.domain.match.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.match.command.domain.match.MatchStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MatchRespondedEvent extends Event {

  private final long requesterId;
  private final long responderId;
  private final String matchStatus;
  private final String contactType;

  public static MatchRespondedEvent of(
      long requesterId, long responderId, MatchStatus matchStatus, String contactType) {
    return new MatchRespondedEvent(requesterId, responderId, matchStatus.toString(), contactType);
  }
}
