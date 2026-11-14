package kr.co.deepple.payment.adapter.out.store;

import com.google.api.services.androidpublisher.model.ProductPurchase;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Google Play에 요청하지 않고 모든 구매를 유효한 미소비 구매로 처리한다. */
@Slf4j
@Component
@ConditionalOnProperty(name = "payment.google-play.enabled", havingValue = "false")
public class FakeGooglePlayClient implements GooglePlayClient {

  private static final int PURCHASE_STATE_PURCHASED = 0;

  @Override
  public ProductPurchase verifyPurchase(@NonNull String productId, @NonNull String purchaseToken) {
    log.info("[Google Play 구매 검증] productId={}, purchaseToken={}", productId, purchaseToken);
    return new ProductPurchase()
        .setOrderId("LOCAL-" + UUID.randomUUID())
        .setPurchaseState(PURCHASE_STATE_PURCHASED)
        .setQuantity(DEFAULT_QUANTITY);
  }

  @Override
  public void consumePurchase(@NonNull String productId, @NonNull String purchaseToken) {
    log.info("[Google Play 구매 소비] productId={}, purchaseToken={}", productId, purchaseToken);
  }
}
