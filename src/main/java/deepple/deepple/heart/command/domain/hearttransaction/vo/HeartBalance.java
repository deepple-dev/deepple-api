package deepple.deepple.heart.command.domain.hearttransaction.vo;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.heart.command.domain.HeartErrorCode;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@EqualsAndHashCode
public final class HeartBalance {
    private static final Long MIN_HEART_BALANCE = 0L;
    @Getter
    private final Long purchaseHeartBalance;
    @Getter
    private final Long missionHeartBalance;

    protected HeartBalance() {
        this.purchaseHeartBalance = MIN_HEART_BALANCE;
        this.missionHeartBalance = MIN_HEART_BALANCE;
    }

    private HeartBalance(Long purchaseHeartBalance, Long missionHeartBalance) {
        validateMinHeartBalance(purchaseHeartBalance, missionHeartBalance);
        this.purchaseHeartBalance = purchaseHeartBalance;
        this.missionHeartBalance = missionHeartBalance;
    }

    public static HeartBalance init() {
        return new HeartBalance();
    }

    public static HeartBalance of(Long missionHeartBalance, Long purchaseHeartBalance) {
        return new HeartBalance(purchaseHeartBalance, missionHeartBalance);
    }

    public long getTotalHeartBalance() {
        return purchaseHeartBalance + missionHeartBalance;
    }

    public HeartBalance useHeart(long amount) {
        validatePositiveAmount(amount);
        validateBalanceIsUsable(amount);
        long usedMissionHeart = Math.min(missionHeartBalance, amount);
        long usedPurchaseHeart = amount - usedMissionHeart;
        return new HeartBalance(purchaseHeartBalance - usedPurchaseHeart, missionHeartBalance - usedMissionHeart);
    }

    public HeartBalance gainPurchaseHeart(long amount) {
        validatePositiveAmount(amount);
        return new HeartBalance(purchaseHeartBalance + amount, missionHeartBalance);
    }

    public HeartBalance gainMissionHeart(long amount) {
        validatePositiveAmount(amount);
        return new HeartBalance(purchaseHeartBalance, missionHeartBalance + amount);
    }

    public HeartBalance refundPurchaseHeart(long amount) {
        validatePositiveAmount(amount);
        return new HeartBalance(Math.max(purchaseHeartBalance - amount, MIN_HEART_BALANCE), missionHeartBalance);
    }

    private void validatePositiveAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("하트 수량은 양수여야 합니다. amount: " + amount);
        }
    }

    private void validateBalanceIsUsable(long amount) {
        long totalHeartBalance = getTotalHeartBalance();
        if (totalHeartBalance < amount) {
            throw new DeeppleException(
                HeartErrorCode.INSUFFICIENT_HEART_BALANCE,
                "balance=" + totalHeartBalance + ", amount=" + amount
            );
        }
    }

    private void validateMinHeartBalance(Long purchaseHeartBalance, Long missionHeartBalance) {
        if (purchaseHeartBalance < MIN_HEART_BALANCE) {
            throw new IllegalArgumentException("하트 잔액이 최소값 미만입니다. purchaseHeartBalance: " + purchaseHeartBalance);
        }
        if (missionHeartBalance < MIN_HEART_BALANCE) {
            throw new IllegalArgumentException("하트 잔액이 최소값 미만입니다. missionHeartBalance: " + missionHeartBalance);
        }
    }
}
