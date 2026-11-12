package kr.co.deepple.heart.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public final class HeartAmount {

  private final long amount;

  private HeartAmount(long amount) {
    this.amount = amount;
  }

  public static HeartAmount of(@NonNull TransactionType transactionType, long amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException("하트 거래량은 양수여야 합니다. amount: " + amount);
    }
    if (transactionType.isDeductingType()) {
      return new HeartAmount(-amount);
    }
    if (transactionType.isAddingType()) {
      return new HeartAmount(amount);
    }
    throw new IllegalArgumentException(
        "차감·획득 어느 쪽도 아닌 거래 종류입니다. transactionType: " + transactionType);
  }
}
