package deepple.deepple.heart.command.domain.hearttransaction;

import deepple.deepple.common.entity.BaseEntity;
import deepple.deepple.heart.command.domain.hearttransaction.vo.HeartAmount;
import deepple.deepple.heart.command.domain.hearttransaction.vo.HeartBalance;
import deepple.deepple.heart.command.domain.hearttransaction.vo.TransactionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "heart_transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HeartTransaction extends BaseEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Getter
    private Long id;

    @Getter
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    @Getter
    private TransactionType transactionType;

    @Getter
    private String content;

    @Embedded
    @Getter
    private HeartAmount heartAmount;

    @Embedded
    @Getter
    private HeartBalance heartBalance;

    private HeartTransaction(Long memberId, TransactionType transactionType, String content, long amount,
        HeartBalance heartBalance) {
        setMemberId(memberId);
        setTransactionType(transactionType);
        setContent(content);
        setHeartAmount(amount);
        setHeartBalance(heartBalance);
    }

    public static HeartTransaction of(Long memberId, TransactionType transactionType, String content, long amount,
        HeartBalance heartBalance) {
        return new HeartTransaction(memberId, transactionType, content, amount, heartBalance);
    }

    private void setMemberId(@NonNull Long memberId) {
        this.memberId = memberId;
    }

    private void setTransactionType(@NonNull TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    private void setContent(@NonNull String content) {
        if (content.isBlank()) {
            throw new IllegalArgumentException("content는 비어있을 수 없습니다.");
        }
        this.content = content;
    }

    private void setHeartAmount(long amount) {
        this.heartAmount = HeartAmount.of(transactionType, amount);
    }

    private void setHeartBalance(@NonNull HeartBalance heartBalance) {
        this.heartBalance = heartBalance;
    }
}
