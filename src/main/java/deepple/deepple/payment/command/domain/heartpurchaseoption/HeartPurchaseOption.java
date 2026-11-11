package deepple.deepple.payment.command.domain.heartpurchaseoption;

import deepple.deepple.common.entity.SoftDeleteBaseEntity;
import deepple.deepple.common.event.Events;
import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.payment.command.domain.PaymentErrorCode;
import deepple.deepple.payment.command.domain.order.event.HeartPurchasedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "heart_purchase_options")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE heart_purchase_options SET deleted_at = now() WHERE id = ?")
public class HeartPurchaseOption extends SoftDeleteBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Embedded
    private HeartPurchaseAmount amount;

    @Embedded
    @Getter
    private Price price;

    @Getter
    private String name;

    @Getter
    private String productId;

    private HeartPurchaseOption(HeartPurchaseAmount amount, Price price, String productId, String name) {
        setAmount(amount);
        setPrice(price);
        setProductId(productId);
        setName(name);
    }

    public static HeartPurchaseOption of(HeartPurchaseAmount amount, Price price, String productId, String name) {
        return new HeartPurchaseOption(amount, price, productId, name);
    }

    public Long getHeartAmount() {
        return amount.getAmount();
    }

    public void purchase(long memberId, long orderId, int quantity) {
        long heartAmount = getHeartAmount() * quantity;
        Events.raise(HeartPurchasedEvent.of(memberId, orderId, heartAmount));
    }

    private void setAmount(@NonNull HeartPurchaseAmount amount) {
        this.amount = amount;
    }

    private void setPrice(@NonNull Price price) {
        this.price = price;
    }

    private void setProductId(@NonNull String productId) {
        if (productId.isBlank()) {
            throw new DeeppleException(PaymentErrorCode.INVALID_HEART_PURCHASE_OPTION, "productId 값이 비어있습니다.");
        }
        this.productId = productId;
    }

    private void setName(@NonNull String name) {
        if (name.isBlank()) {
            throw new DeeppleException(PaymentErrorCode.INVALID_HEART_PURCHASE_OPTION, "name 값이 비어있습니다.");
        }
        this.name = name;
    }
}
