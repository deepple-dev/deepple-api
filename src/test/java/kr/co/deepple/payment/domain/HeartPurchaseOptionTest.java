package kr.co.deepple.payment.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.payment.fixture.HeartPurchaseOptionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HeartPurchaseOptionTest {

  @ParameterizedTest
  @ValueSource(strings = {"name is blank", "productId is blank"})
  @DisplayName("name, productId 이 blank 이면 예외가 발생한다.")
  void throwsExceptionWhenNameOrProductIdIsBlank(String fieldName) {
    // given
    HeartPurchaseAmount amount = HeartPurchaseAmount.from(10L);
    Price price = Price.from(1000L);
    String productId = fieldName.equals("productId is blank") ? " " : "productId";
    String name = fieldName.equals("name is blank") ? " " : "name";

    // when, then
    assertThatThrownBy(() -> HeartPurchaseOption.of(amount, price, productId, name))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.INVALID_HEART_PURCHASE_OPTION);
  }

  @Test
  @DisplayName("amount, price, name 값이 주어지면 객체가 생성된다.")
  void createObjectWhenAmountPriceNameAreGiven() {
    // given
    HeartPurchaseAmount amount = HeartPurchaseAmount.from(10L);
    Price price = Price.from(1000L);
    String productId = "productId";
    String name = "name";

    // when
    HeartPurchaseOption heartPurchaseOption =
        HeartPurchaseOption.of(amount, price, productId, name);

    // then
    assertThat(heartPurchaseOption).isNotNull();
  }

  @Test
  @DisplayName("수량만큼 곱한 하트 수를 계산한다")
  void calculateHeartAmountByQuantity() {
    // given
    Long amount = 10L;
    HeartPurchaseOption heartPurchaseOption =
        HeartPurchaseOptionFixture.builder().amount(amount).build();
    int quantity = 5;

    // when
    long heartAmount = heartPurchaseOption.calculateHeartAmount(quantity);

    // then
    assertThat(heartAmount).isEqualTo(amount * quantity);
  }
}
