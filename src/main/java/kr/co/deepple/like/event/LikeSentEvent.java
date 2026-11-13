package kr.co.deepple.like.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record LikeSentEvent(
    long senderId, @NonNull String senderName, long receiverId, @NonNull EventMetadata metadata)
    implements Event {

  public static LikeSentEvent of(long senderId, String senderName, long receiverId) {
    return new LikeSentEvent(senderId, senderName, receiverId, EventMetadata.create());
  }
}
