package deepple.deepple.payment.command.infra.order;

import deepple.deepple.payment.command.application.order.OrderService;
import deepple.deepple.payment.command.domain.order.PaymentMethod;
import deepple.deepple.payment.command.domain.order.event.AppStoreReceiptVerifiedEvent;
import deepple.deepple.payment.command.domain.order.event.GooglePlayReceiptVerifiedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReceiptVerifiedEventHandler {
    private final OrderService orderService;

    @EventListener(value = AppStoreReceiptVerifiedEvent.class)
    public void handle(AppStoreReceiptVerifiedEvent event) {
        orderService.processReceipt(event.getMemberId(), event.getTransactionId(), event.getProductId(),
            event.getQuantity(), PaymentMethod.APP_STORE);
    }

    @EventListener(value = GooglePlayReceiptVerifiedEvent.class)
    public void handle(GooglePlayReceiptVerifiedEvent event) {
        orderService.processReceipt(event.getMemberId(), event.getTransactionId(), event.getProductId(),
            event.getQuantity(), PaymentMethod.GOOGLE_PLAY);
    }
}
