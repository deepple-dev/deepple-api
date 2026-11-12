package kr.co.deepple.heart.domain;

import jakarta.persistence.Embeddable;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public final class HeartBalance {

  private static final long MIN_BALANCE = 0L;

  private final long missionBalance;
  private final long purchaseBalance;

  private HeartBalance(long missionBalance, long purchaseBalance) {
    if (missionBalance < MIN_BALANCE) {
      throw new IllegalArgumentException("하트 잔액이 최소값 미만입니다. missionBalance: " + missionBalance);
    }
    if (purchaseBalance < MIN_BALANCE) {
      throw new IllegalArgumentException("하트 잔액이 최소값 미만입니다. purchaseBalance: " + purchaseBalance);
    }
    this.missionBalance = missionBalance;
    this.purchaseBalance = purchaseBalance;
  }

  public static HeartBalance init() {
    return new HeartBalance(MIN_BALANCE, MIN_BALANCE);
  }

  public static HeartBalance of(long missionBalance, long purchaseBalance) {
    return new HeartBalance(missionBalance, purchaseBalance);
  }

  public long getTotalBalance() {
    return missionBalance + purchaseBalance;
  }

  public HeartBalance apply(TransactionType transactionType, long amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException("하트 수량은 양수여야 합니다. amount: " + amount);
    }
    return switch (transactionType) {
      case MESSAGE, MESSAGE_ACCEPTED, DATING_EXAM, PROFILE_EXCHANGE, INTRODUCTION -> use(amount);
      case PURCHASE -> addPurchase(amount);
      case MISSION, ADMIN_GRANT -> addMission(amount);
      case REFUND -> refund(amount);
    };
  }

  private HeartBalance use(long amount) {
    long totalBalance = getTotalBalance();
    if (totalBalance < amount) {
      throw new DeeppleException(
          HeartErrorCode.INSUFFICIENT_HEART_BALANCE,
          "balance=" + totalBalance + ", amount=" + amount);
    }
    long usedMission = Math.min(missionBalance, amount);
    long usedPurchase = amount - usedMission;
    return new HeartBalance(missionBalance - usedMission, purchaseBalance - usedPurchase);
  }

  private HeartBalance addPurchase(long amount) {
    return new HeartBalance(missionBalance, purchaseBalance + amount);
  }

  private HeartBalance addMission(long amount) {
    return new HeartBalance(missionBalance + amount, purchaseBalance);
  }

  private HeartBalance refund(long amount) {
    return new HeartBalance(missionBalance, Math.max(purchaseBalance - amount, MIN_BALANCE));
  }
}
