package kr.co.deepple.payment.domain;

import jakarta.persistence.Column;
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
public class Price {

  private static final long MIN_PRICE = 1L;

  @Column(name = "price")
  private final long value;

  private Price(long value) {
    if (value < MIN_PRICE) {
      throw new DeeppleException(
          PaymentErrorCode.INVALID_HEART_PURCHASE_OPTION, "price 값이 최소값보다 낮습니다. price=" + value);
    }
    this.value = value;
  }

  public static Price from(long value) {
    return new Price(value);
  }
}
