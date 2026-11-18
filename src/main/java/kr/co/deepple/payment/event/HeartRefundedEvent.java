package kr.co.deepple.payment.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;
import org.springframework.modulith.events.Externalized;

@Externalized(PaymentTopic.NAME + "::#{#this.memberId()}")
public record HeartRefundedEvent(
    long memberId,
    long orderId,
    long amount,
    @NonNull String transactionId,
    @NonNull EventMetadata metadata)
    implements Event {

  public static HeartRefundedEvent of(
      long memberId, long orderId, long amount, String transactionId) {
    return new HeartRefundedEvent(memberId, orderId, amount, transactionId, EventMetadata.create());
  }
}
