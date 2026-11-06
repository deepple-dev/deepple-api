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

    private HeartTransaction(Long memberId, TransactionType transactionType, String content, HeartAmount heartAmount,
        HeartBalance heartBalance) {
        setMemberId(memberId);
        setTransactionType(transactionType);
        setContent(content);
        setHeartAmount(heartAmount);
        setHeartBalance(heartBalance);
        validateHeartTransaction(transactionType, heartAmount);
    }

    public static HeartTransaction of(Long memberId, TransactionType transactionType, String content,
        HeartAmount heartAmount, HeartBalance heartBalance) {
        return new HeartTransaction(memberId, transactionType, content, heartAmount, heartBalance);
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

    private void setHeartAmount(@NonNull HeartAmount heartAmount) {
        this.heartAmount = heartAmount;
    }

    private void setHeartBalance(@NonNull HeartBalance heartBalance) {
        this.heartBalance = heartBalance;
    }

    private void validateHeartTransaction(TransactionType transactionType, HeartAmount heartAmount) {
        if (transactionType.isUsingType() && !heartAmount.isUsingAmount()) {
            throw new IllegalArgumentException(
                "잘못된 하트 사용량 입니다. transactionType: " + transactionType + ", heartAmount: " + heartAmount);
        }
        if (transactionType.isGainingType() && !heartAmount.isGainingAmount()) {
            throw new IllegalArgumentException(
                "잘못된 하트 획득량 입니다. transactionType: " + transactionType + ", heartAmount: " + heartAmount);
        }
    }
}
