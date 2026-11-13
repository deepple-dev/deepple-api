package kr.co.deepple.payment.command.domain.order.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record GooglePlayReceiptVerifiedEvent(
    long memberId,
    String transactionId,
    String productId,
    int quantity,
    @NonNull EventMetadata metadata)
    implements Event {

  public static GooglePlayReceiptVerifiedEvent of(
      long memberId, String transactionId, String productId, int quantity) {
    return new GooglePlayReceiptVerifiedEvent(
        memberId, transactionId, productId, quantity, EventMetadata.create());
  }
}
