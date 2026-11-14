package kr.co.deepple.payment.adapter.out.store;

import com.google.api.services.androidpublisher.model.ProductPurchase;
import lombok.NonNull;

public interface GooglePlayClient {
  int DEFAULT_QUANTITY = 1;

  ProductPurchase verifyPurchase(@NonNull String productId, @NonNull String purchaseToken);

  void consumePurchase(@NonNull String productId, @NonNull String purchaseToken);

  default int resolveQuantity(ProductPurchase purchase) {
    Integer quantity = purchase.getQuantity();
    return quantity == null ? DEFAULT_QUANTITY : quantity;
  }
}
