package deepple.deepple.payment.command.infra.order;

import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import deepple.deepple.payment.command.infra.order.exception.GooglePlayClientException;
import deepple.deepple.payment.command.infra.order.exception.InvalidGooglePlayReceiptException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

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
        try {
            androidPublisher.purchases().products()
                .consume(packageName, productId, purchaseToken)
                .execute();
        } catch (IOException e) {
            throw new GooglePlayClientException(e);
        }
    }

    public int resolveQuantity(ProductPurchase purchase) {
        Integer quantity = purchase.getQuantity();
        return quantity == null ? DEFAULT_QUANTITY : quantity;
    }

    private ProductPurchase getProductPurchase(String productId, String purchaseToken) {
        try {
            return androidPublisher.purchases().products()
                .get(packageName, productId, purchaseToken)
                .execute();
        } catch (IOException e) {
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
