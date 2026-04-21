package deepple.deepple.payment.command.application.order;

import com.google.api.services.androidpublisher.model.ProductPurchase;
import deepple.deepple.common.event.Events;
import deepple.deepple.payment.command.domain.order.event.GooglePlayReceiptVerifiedEvent;
import deepple.deepple.payment.command.infra.order.GooglePlayClient;
import deepple.deepple.payment.command.infra.order.exception.GooglePlayClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GooglePlayPaymentService {
    private final GooglePlayClient googlePlayClient;

    public void verifyReceipt(String productId, String purchaseToken, Long memberId) {
        long start = System.currentTimeMillis();
        log.info("[GooglePlay] verifyReceipt 시작. memberId={}, productId={}, tokenPrefix={}",
            memberId, productId, purchaseToken.substring(0, Math.min(20, purchaseToken.length())));

        ProductPurchase purchase = googlePlayClient.verifyPurchase(productId, purchaseToken);
        log.info("[GooglePlay] verifyPurchase 완료. elapsedMs={}, orderId={}, purchaseState={}, consumptionState={}, quantity={}",
            System.currentTimeMillis() - start,
            purchase.getOrderId(), purchase.getPurchaseState(), purchase.getConsumptionState(), purchase.getQuantity());

        raiseReceiptVerifiedEvent(memberId, productId, purchase);
        log.info("[GooglePlay] 이벤트 발행 완료. elapsedMs={}, orderId={}",
            System.currentTimeMillis() - start, purchase.getOrderId());

        consumeSafely(productId, purchaseToken, purchase.getOrderId());
        log.info("[GooglePlay] verifyReceipt 종료. totalElapsedMs={}, orderId={}",
            System.currentTimeMillis() - start, purchase.getOrderId());
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
        long consumeStart = System.currentTimeMillis();
        try {
            googlePlayClient.consumePurchase(productId, purchaseToken);
            log.info("[GooglePlay] consume 성공. elapsedMs={}, orderId={}, productId={}",
                System.currentTimeMillis() - consumeStart, orderId, productId);
        } catch (GooglePlayClientException e) {
            log.error("[GooglePlay] consume 실패 - 3일 내 수동 조치 필요. elapsedMs={}, orderId={}, productId={}",
                System.currentTimeMillis() - consumeStart, orderId, productId, e);
        }
    }
}
