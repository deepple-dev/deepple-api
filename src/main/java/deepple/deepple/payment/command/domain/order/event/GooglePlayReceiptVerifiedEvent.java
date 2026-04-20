package deepple.deepple.payment.command.domain.order.event;

import deepple.deepple.common.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GooglePlayReceiptVerifiedEvent extends Event {
    private final long memberId;
    private final String transactionId;
    private final String productId;
    private final int quantity;

    public static GooglePlayReceiptVerifiedEvent of(long memberId, String transactionId, String productId,
        int quantity) {
        return new GooglePlayReceiptVerifiedEvent(memberId, transactionId, productId, quantity);
    }
}
