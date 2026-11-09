package deepple.deepple.heart.fixture;

import deepple.deepple.heart.command.domain.hearttransaction.HeartTransaction;
import deepple.deepple.heart.command.domain.hearttransaction.vo.HeartBalance;
import deepple.deepple.heart.command.domain.hearttransaction.vo.TransactionType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HeartTransactionFixture {

    private Long id;
    private long memberId = 1L;
    private TransactionType transactionType = TransactionType.MESSAGE;
    private String content = TransactionType.MESSAGE.getDescription();
    private long amount = 20L;
    private long missionHeartBalance;
    private long purchaseHeartBalance;

    public static HeartTransactionFixture builder() {
        return new HeartTransactionFixture();
    }

    public HeartTransaction build() {
        HeartTransaction transaction = HeartTransaction.of(memberId, transactionType, content,
            amount, HeartBalance.of(missionHeartBalance, purchaseHeartBalance));
        setField(transaction, "id", id);
        return transaction;
    }
}
