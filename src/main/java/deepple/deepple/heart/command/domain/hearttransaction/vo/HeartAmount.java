package deepple.deepple.heart.command.domain.hearttransaction.vo;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Embeddable
@EqualsAndHashCode
public final class HeartAmount {
    @Getter
    private final Long amount;

    protected HeartAmount() {
        this.amount = 0L;
    }

    private HeartAmount(long amount) {
        this.amount = amount;
    }

    public static HeartAmount of(@NonNull TransactionType transactionType, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("하트 거래량은 양수여야 합니다. amount: " + amount);
        }
        if (transactionType.isUsingType()) {
            return new HeartAmount(-amount);
        }
        if (transactionType.isGainingType()) {
            return new HeartAmount(amount);
        }
        throw new IllegalArgumentException("사용·획득 어느 쪽도 아닌 거래 종류입니다. transactionType: " + transactionType);
    }
}
