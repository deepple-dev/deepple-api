package kr.co.deepple.community.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record ProfileExchangeAcceptedEvent(
    long profileExchangeId,
    long requesterId,
    long responderId,
    String senderName,
    @NonNull EventMetadata metadata)
    implements Event {

  public static ProfileExchangeAcceptedEvent of(
      long profileExchangeId, long requesterId, long responderId, String senderName) {
    return new ProfileExchangeAcceptedEvent(
        profileExchangeId, requesterId, responderId, senderName, EventMetadata.create());
  }
}
