package kr.co.deepple.payment.domain;

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
public class HeartPurchaseAmount {

  private static final long MIN_AMOUNT = 1L;

  private final long amount;

  private HeartPurchaseAmount(long amount) {
    if (amount < MIN_AMOUNT) {
      throw new DeeppleException(
          PaymentErrorCode.INVALID_HEART_PURCHASE_OPTION, "amount 값이 최소값보다 낮습니다. amount=" + amount);
    }
    this.amount = amount;
  }

  public static HeartPurchaseAmount from(long amount) {
    return new HeartPurchaseAmount(amount);
  }
}
