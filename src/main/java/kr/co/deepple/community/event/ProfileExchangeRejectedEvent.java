package kr.co.deepple.community.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record ProfileExchangeRejectedEvent(
    long requesterId, long responderId, String senderName, @NonNull EventMetadata metadata)
    implements Event {

  public static ProfileExchangeRejectedEvent of(
      long requesterId, long responderId, String senderName) {
    return new ProfileExchangeRejectedEvent(
        requesterId, responderId, senderName, EventMetadata.create());
  }
}
