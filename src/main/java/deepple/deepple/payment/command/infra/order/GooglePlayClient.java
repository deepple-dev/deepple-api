package deepple.deepple.payment.command.infra.order;

import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import deepple.deepple.payment.command.infra.order.exception.GooglePlayClientException;
import deepple.deepple.payment.command.infra.order.exception.InvalidGooglePlayReceiptException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GooglePlayClient {

    private static final int PURCHASE_STATE_PURCHASED = 0;
    private static final int CONSUMPTION_STATE_CONSUMED = 1;
    private static final int DEFAULT_QUANTITY = 1;

    private final AndroidPublisher androidPublisher;

    @Value("${payment.google-play.package-name}")
    private String packageName;

    public ProductPurchase verifyPurchase(@NonNull String productId, @NonNull String purchaseToken) {
        ProductPurchase purchase = getProductPurchase(productId, purchaseToken);
        verifyPurchaseState(purchase);
        verifyConsumptionState(purchase);
        return purchase;
    }

    public void consumePurchase(@NonNull String productId, @NonNull String purchaseToken) {
        long start = System.currentTimeMillis();
        log.info("[GooglePlay] purchases.products.consume 호출. packageName={}, productId={}", packageName, productId);
        try {
            androidPublisher.purchases().products()
                .consume(packageName, productId, purchaseToken)
                .execute();
            log.info("[GooglePlay] purchases.products.consume 응답. elapsedMs={}", System.currentTimeMillis() - start);
        } catch (IOException e) {
            log.error("[GooglePlay] purchases.products.consume 실패. elapsedMs={}, packageName={}, productId={}",
                System.currentTimeMillis() - start, packageName, productId, e);
            throw new GooglePlayClientException(e);
        }
    }

    public int resolveQuantity(ProductPurchase purchase) {
        Integer quantity = purchase.getQuantity();
        return quantity == null ? DEFAULT_QUANTITY : quantity;
    }

    private ProductPurchase getProductPurchase(String productId, String purchaseToken) {
        long start = System.currentTimeMillis();
        log.info("[GooglePlay] purchases.products.get 호출. packageName={}, productId={}", packageName, productId);
        try {
            ProductPurchase response = androidPublisher.purchases().products()
                .get(packageName, productId, purchaseToken)
                .execute();
            log.info("[GooglePlay] purchases.products.get 응답. elapsedMs={}, orderId={}",
                System.currentTimeMillis() - start, response.getOrderId());
            return response;
        } catch (IOException e) {
            log.error("[GooglePlay] purchases.products.get 실패. elapsedMs={}, packageName={}, productId={}",
                System.currentTimeMillis() - start, packageName, productId, e);
            throw new GooglePlayClientException(e);
        }
    }

    private void verifyPurchaseState(ProductPurchase purchase) {
        Integer purchaseState = purchase.getPurchaseState();
        if (purchaseState == null || purchaseState != PURCHASE_STATE_PURCHASED) {
            throw new InvalidGooglePlayReceiptException(
                "유효하지 않은 Google Play 구매 상태입니다. purchaseState=" + purchaseState);
        }
    }

    private void verifyConsumptionState(ProductPurchase purchase) {
        Integer consumptionState = purchase.getConsumptionState();
        if (consumptionState != null && consumptionState == CONSUMPTION_STATE_CONSUMED) {
            throw new InvalidGooglePlayReceiptException("이미 소비된 Google Play 구매입니다.");
        }
    }
}
