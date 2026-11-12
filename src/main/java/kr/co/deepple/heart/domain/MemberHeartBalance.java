package kr.co.deepple.heart.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
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
@Table(name = "heart_balances")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberHeartBalance extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long memberId;

    @Embedded
    private HeartBalance balance;

    private MemberHeartBalance(long memberId, @NonNull HeartBalance balance) {
        this.memberId = memberId;
        this.balance = balance;
    }

    public static MemberHeartBalance of(long memberId, HeartBalance balance) {
        return new MemberHeartBalance(memberId, balance);
    }

    public long getMissionBalance() {
        return balance.getMissionBalance();
    }

    public long getPurchaseBalance() {
        return balance.getPurchaseBalance();
    }

    public long getTotalBalance() {
        return balance.getTotalBalance();
    }

    public void apply(@NonNull HeartTransaction transaction) {
        if (transaction.getMemberId() != memberId) {
            throw new IllegalArgumentException(
                "다른 회원의 거래 내역입니다. memberId=" + memberId + ", transactionMemberId=" + transaction.getMemberId());
        }
        this.balance = transaction.getBalance();
    }
}
