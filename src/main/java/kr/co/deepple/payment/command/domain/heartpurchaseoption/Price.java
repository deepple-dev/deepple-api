package kr.co.deepple.payment.command.domain.heartpurchaseoption;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.payment.command.domain.PaymentErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Embeddable
@EqualsAndHashCode
public class Price {
    private static final Long MIN_PRICE = 1L;

    @Getter
    @Column(name = "price")
    private final Long value;

    protected Price() {
        this.value = MIN_PRICE;
    }

    private Price(@NonNull Long value) {
        validateMinPrice(value);
        this.value = value;
    }

    public static Price from(Long value) {
        return new Price(value);
    }

    private void validateMinPrice(Long value) {
        if (value < MIN_PRICE) {
            throw new DeeppleException(
                PaymentErrorCode.INVALID_HEART_PURCHASE_OPTION,
                "price 값이 최소값보다 낮습니다. price=" + value
            );
        }
    }
}
