package kr.co.deepple.heart.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "heart_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HeartTransaction extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long memberId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private TransactionType transactionType;

    private String content;

    @Embedded
    @Getter(AccessLevel.NONE)
    private HeartAmount amount;

    @Embedded
    private HeartBalance balance;

    private String idempotencyKey;

    private HeartTransaction(long memberId, @NonNull TransactionType transactionType, @NonNull String content,
        long amount, @NonNull HeartBalance balance, String idempotencyKey) {
        if (content.isBlank()) {
            throw new IllegalArgumentException("content는 비어있을 수 없습니다.");
        }
        this.memberId = memberId;
        this.transactionType = transactionType;
        this.content = content;
        this.amount = HeartAmount.of(transactionType, amount);
        this.balance = balance;
        this.idempotencyKey = idempotencyKey;
    }

    public static HeartTransaction of(long memberId, TransactionType transactionType, String content, long amount,
        HeartBalance balance, String idempotencyKey) {
        return new HeartTransaction(memberId, transactionType, content, amount, balance, idempotencyKey);
    }

    public long getAmount() {
        return amount.getAmount();
    }
}
