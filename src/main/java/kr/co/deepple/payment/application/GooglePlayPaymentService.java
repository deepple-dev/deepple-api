package kr.co.deepple.payment.application;

import com.google.api.services.androidpublisher.model.ProductPurchase;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.payment.adapter.out.store.GooglePlayClient;
import kr.co.deepple.payment.application.port.out.OrderCommandRepository;
import kr.co.deepple.payment.domain.GooglePlayReceiptVerifiedEvent;
import kr.co.deepple.payment.domain.PaymentErrorCode;
import kr.co.deepple.payment.domain.PaymentMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GooglePlayPaymentService {

  private static final int CONSUMPTION_STATE_CONSUMED = 1;

  private final GooglePlayClient googlePlayClient;
  private final OrderCommandRepository orderCommandRepository;
  private final ApplicationEventPublisher eventPublisher;

  public void verifyReceipt(String productId, String purchaseToken, long memberId) {
    ProductPurchase purchase = googlePlayClient.verifyPurchase(productId, purchaseToken);
    if (isConsumed(purchase)) {
      verifyAlreadyProcessed(purchase.getOrderId(), memberId);
      return;
    }
    raiseReceiptVerifiedEvent(memberId, productId, purchase);
    consumeSafely(productId, purchaseToken, purchase.getOrderId());
  }

  private boolean isConsumed(ProductPurchase purchase) {
    Integer consumptionState = purchase.getConsumptionState();
    return consumptionState != null && consumptionState == CONSUMPTION_STATE_CONSUMED;
  }

  private void verifyAlreadyProcessed(String orderId, long memberId) {
    boolean processed =
        orderCommandRepository
            .findByTransactionIdAndPaymentMethod(orderId, PaymentMethod.GOOGLE_PLAY)
            .filter(order -> order.isOrderedBy(memberId))
            .isPresent();
    if (!processed) {
      throw new DeeppleException(
          PaymentErrorCode.INVALID_GOOGLE_PLAY_RECEIPT,
          "이미 소비된 Google Play 구매입니다. orderId=" + orderId);
    }
  }

  private void raiseReceiptVerifiedEvent(
      long memberId, String productId, ProductPurchase purchase) {
    eventPublisher.publishEvent(
        GooglePlayReceiptVerifiedEvent.of(
            memberId,
            purchase.getOrderId(),
            productId,
            googlePlayClient.resolveQuantity(purchase)));
  }

  private void consumeSafely(String productId, String purchaseToken, String orderId) {
    try {
      googlePlayClient.consumePurchase(productId, purchaseToken);
    } catch (DeeppleException e) {
      if (e.getErrorCode() != PaymentErrorCode.GOOGLE_PLAY_UNAVAILABLE) {
        throw e;
      }
      log.atError()
          .setCause(e)
          .addKeyValue("orderId", orderId)
          .addKeyValue("productId", productId)
          .log("Google Play consume 실패, 3일 안에 수동 조치 필요");
    }
  }
}
