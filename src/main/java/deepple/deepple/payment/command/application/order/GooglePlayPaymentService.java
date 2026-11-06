package deepple.deepple.payment.command.application.order;

import com.google.api.services.androidpublisher.model.ProductPurchase;
import deepple.deepple.common.event.Events;
import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.payment.command.domain.PaymentErrorCode;
import deepple.deepple.payment.command.domain.order.event.GooglePlayReceiptVerifiedEvent;
import deepple.deepple.payment.command.infra.order.GooglePlayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GooglePlayPaymentService {
    private final GooglePlayClient googlePlayClient;

    public void verifyReceipt(String productId, String purchaseToken, Long memberId) {
        ProductPurchase purchase = googlePlayClient.verifyPurchase(productId, purchaseToken);
        raiseReceiptVerifiedEvent(memberId, productId, purchase);
        consumeSafely(productId, purchaseToken, purchase.getOrderId());
    }

    private void raiseReceiptVerifiedEvent(Long memberId, String productId, ProductPurchase purchase) {
        Events.raise(
            GooglePlayReceiptVerifiedEvent.of(
                memberId,
                purchase.getOrderId(),
                productId,
                googlePlayClient.resolveQuantity(purchase)
            )
        );
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
