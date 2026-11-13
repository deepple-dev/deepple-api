package kr.co.deepple.payment.command.domain.order.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record HeartPurchasedEvent(
    long memberId, long orderId, long amount, @NonNull EventMetadata metadata) implements Event {

  public static HeartPurchasedEvent of(long memberId, long orderId, long amount) {
    return new HeartPurchasedEvent(memberId, orderId, amount, EventMetadata.create());
  }
}
