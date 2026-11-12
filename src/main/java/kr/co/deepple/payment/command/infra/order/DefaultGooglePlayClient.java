package kr.co.deepple.payment.command.infra.order;

import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import java.io.IOException;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.payment.command.domain.PaymentErrorCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "payment.google-play.enabled",
    havingValue = "true",
    matchIfMissing = true)
@RequiredArgsConstructor
public class DefaultGooglePlayClient implements GooglePlayClient {

  private static final int PURCHASE_STATE_PURCHASED = 0;

  private final AndroidPublisher androidPublisher;

  @Value("${payment.google-play.package-name}")
  private String packageName;

  @Override
  public ProductPurchase verifyPurchase(@NonNull String productId, @NonNull String purchaseToken) {
    ProductPurchase purchase = getProductPurchase(productId, purchaseToken);
    verifyPurchaseState(purchase);
    return purchase;
  }

  @Override
  public void consumePurchase(@NonNull String productId, @NonNull String purchaseToken) {
    try {
      androidPublisher
          .purchases()
          .products()
          .consume(packageName, productId, purchaseToken)
          .execute();
    } catch (IOException e) {
      throw new DeeppleException(PaymentErrorCode.GOOGLE_PLAY_UNAVAILABLE, e.getMessage(), e);
    }
  }

  private ProductPurchase getProductPurchase(String productId, String purchaseToken) {
    try {
      return androidPublisher
          .purchases()
          .products()
          .get(packageName, productId, purchaseToken)
          .execute();
    } catch (IOException e) {
      throw new DeeppleException(PaymentErrorCode.GOOGLE_PLAY_UNAVAILABLE, e.getMessage(), e);
    }
  }

  private void verifyPurchaseState(ProductPurchase purchase) {
    Integer purchaseState = purchase.getPurchaseState();
    if (purchaseState == null || purchaseState != PURCHASE_STATE_PURCHASED) {
      throw new DeeppleException(
          PaymentErrorCode.INVALID_GOOGLE_PLAY_RECEIPT,
          "유효하지 않은 Google Play 구매 상태입니다. purchaseState=" + purchaseState);
    }
  }
}
