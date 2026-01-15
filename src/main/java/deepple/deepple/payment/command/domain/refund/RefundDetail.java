package deepple.deepple.payment.command.domain.refund;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RefundDetail {
    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Long refundAmount;

    private RefundDetail(String productId, Integer quantity, Long refundAmount) {
        validateProductId(productId);
        validateQuantity(quantity);
        validateRefundAmount(refundAmount);
        this.productId = productId;
        this.quantity = quantity;
        this.refundAmount = refundAmount;
    }

    public static RefundDetail of(String productId, Integer quantity, Long refundAmount) {
        return new RefundDetail(productId, quantity, refundAmount);
    }

    private void validateProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId는 null이거나 빈 문자열일 수 없습니다.");
        }
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("quantity는 0보다 커야 합니다. quantity: " + quantity);
        }
    }

    private void validateRefundAmount(Long refundAmount) {
        if (refundAmount == null || refundAmount < 0) {
            throw new IllegalArgumentException("refundAmount는 0 이상이어야 합니다. refundAmount: " + refundAmount);
        }
    }
}