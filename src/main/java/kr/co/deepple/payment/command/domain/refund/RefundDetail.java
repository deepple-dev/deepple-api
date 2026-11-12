package kr.co.deepple.payment.command.domain.refund;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefundDetail {

  @Column(nullable = false)
  private String productId;

  @Column(nullable = false)
  private int quantity;

  @Column(nullable = false)
  private long refundAmount;

  private RefundDetail(String productId, int quantity, long refundAmount) {
    if (productId == null || productId.isBlank()) {
      throw new IllegalArgumentException("productId는 null이거나 빈 문자열일 수 없습니다.");
    }
    if (quantity <= 0) {
      throw new IllegalArgumentException("quantity는 0보다 커야 합니다. quantity: " + quantity);
    }
    if (refundAmount < 0) {
      throw new IllegalArgumentException(
          "refundAmount는 0 이상이어야 합니다. refundAmount: " + refundAmount);
    }
    this.productId = productId;
    this.quantity = quantity;
    this.refundAmount = refundAmount;
  }

  public static RefundDetail of(String productId, int quantity, long refundAmount) {
    return new RefundDetail(productId, quantity, refundAmount);
  }
}
