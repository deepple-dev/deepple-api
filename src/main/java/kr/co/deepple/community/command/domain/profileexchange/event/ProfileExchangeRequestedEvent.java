package kr.co.deepple.community.command.domain.profileexchange.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record ProfileExchangeRequestedEvent(
    long requesterId, long responderId, String senderName, @NonNull EventMetadata metadata)
    implements Event {

  public static ProfileExchangeRequestedEvent of(
      long requesterId, long responderId, String senderName) {
    return new ProfileExchangeRequestedEvent(
        requesterId, responderId, senderName, EventMetadata.create());
  }
}
