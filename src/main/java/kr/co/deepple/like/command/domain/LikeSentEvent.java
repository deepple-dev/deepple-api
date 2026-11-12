package kr.co.deepple.like.command.domain;

import kr.co.deepple.common.event.Event;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LikeSentEvent extends Event {

  private final long senderId;
  private final String senderName;
  private final long receiverId;

  public static LikeSentEvent of(long senderId, @NonNull String senderName, long receiverId) {
    return new LikeSentEvent(senderId, senderName, receiverId);
  }
}
