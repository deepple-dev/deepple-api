package kr.co.deepple.payment.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.SoftDeleteBaseEntity;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "heart_purchase_options")
@SQLDelete(sql = "UPDATE heart_purchase_options SET deleted_at = now() WHERE id = ?")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HeartPurchaseOption extends SoftDeleteBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Embedded
  @Getter(AccessLevel.NONE)
  private HeartPurchaseAmount amount;

  @Embedded private Price price;

  private String name;

  private String productId;

  private HeartPurchaseOption(
      @NonNull HeartPurchaseAmount amount,
      @NonNull Price price,
      @NonNull String productId,
      @NonNull String name) {
    if (productId.isBlank()) {
      throw new DeeppleException(
          PaymentErrorCode.INVALID_HEART_PURCHASE_OPTION, "productId 값이 비어있습니다.");
    }
    if (name.isBlank()) {
      throw new DeeppleException(PaymentErrorCode.INVALID_HEART_PURCHASE_OPTION, "name 값이 비어있습니다.");
    }
    this.amount = amount;
    this.price = price;
    this.productId = productId;
    this.name = name;
  }

  public static HeartPurchaseOption of(
      HeartPurchaseAmount amount, Price price, String productId, String name) {
    return new HeartPurchaseOption(amount, price, productId, name);
  }

  public long getHeartAmount() {
    return amount.getAmount();
  }

  public long calculateHeartAmount(int quantity) {
    return getHeartAmount() * quantity;
  }
}
