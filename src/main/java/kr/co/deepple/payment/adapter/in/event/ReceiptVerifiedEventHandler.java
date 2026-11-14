package kr.co.deepple.payment.adapter.in.event;

import kr.co.deepple.payment.application.OrderService;
import kr.co.deepple.payment.domain.AppStoreReceiptVerifiedEvent;
import kr.co.deepple.payment.domain.GooglePlayReceiptVerifiedEvent;
import kr.co.deepple.payment.domain.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReceiptVerifiedEventHandler {

  private final OrderService orderService;

  @EventListener(value = AppStoreReceiptVerifiedEvent.class)
  public void handle(AppStoreReceiptVerifiedEvent event) {
    orderService.processReceipt(
        event.memberId(),
        event.transactionId(),
        event.productId(),
        event.quantity(),
        PaymentMethod.APP_STORE);
  }

  @EventListener(value = GooglePlayReceiptVerifiedEvent.class)
  public void handle(GooglePlayReceiptVerifiedEvent event) {
    orderService.processReceipt(
        event.memberId(),
        event.transactionId(),
        event.productId(),
        event.quantity(),
        PaymentMethod.GOOGLE_PLAY);
  }
}
