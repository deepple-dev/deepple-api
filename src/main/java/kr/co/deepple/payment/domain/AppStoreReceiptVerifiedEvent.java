package kr.co.deepple.payment.domain;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record AppStoreReceiptVerifiedEvent(
    long memberId,
    String transactionId,
    String productId,
    int quantity,
    @NonNull EventMetadata metadata)
    implements Event {

  public static AppStoreReceiptVerifiedEvent of(
      long memberId, String transactionId, String productId, int quantity) {
    return new AppStoreReceiptVerifiedEvent(
        memberId, transactionId, productId, quantity, EventMetadata.create());
  }
}
