package kr.co.deepple.community.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record ProfileExchangeAcceptedEvent(
    long requesterId, long responderId, String senderName, @NonNull EventMetadata metadata)
    implements Event {

  public static ProfileExchangeAcceptedEvent of(
      long requesterId, long responderId, String senderName) {
    return new ProfileExchangeAcceptedEvent(
        requesterId, responderId, senderName, EventMetadata.create());
  }
}
