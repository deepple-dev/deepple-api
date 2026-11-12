package kr.co.deepple.community.command.domain.profileexchange.event;

import kr.co.deepple.common.event.Event;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileExchangeAcceptedEvent extends Event {

  private final long requesterId;
  private final long responderId;
  private final String senderName;

  public static ProfileExchangeAcceptedEvent of(
      long requesterId, long responderId, String senderName) {
    return new ProfileExchangeAcceptedEvent(requesterId, responderId, senderName);
  }
}
