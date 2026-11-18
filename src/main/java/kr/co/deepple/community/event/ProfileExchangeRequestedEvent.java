package kr.co.deepple.community.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record ProfileExchangeRequestedEvent(
    long profileExchangeId,
    long requesterId,
    long responderId,
    String senderName,
    @NonNull EventMetadata metadata)
    implements Event {

  public static ProfileExchangeRequestedEvent of(
      long profileExchangeId, long requesterId, long responderId, String senderName) {
    return new ProfileExchangeRequestedEvent(
        profileExchangeId, requesterId, responderId, senderName, EventMetadata.create());
  }
}
