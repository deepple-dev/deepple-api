package kr.co.deepple.heart.domain.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record AdminHeartGrantedEvent(
    long memberId, long adminId, long amount, @NonNull EventMetadata metadata) implements Event {

  public static AdminHeartGrantedEvent of(long memberId, long adminId, long amount) {
    return new AdminHeartGrantedEvent(memberId, adminId, amount, EventMetadata.create());
  }
}
