package kr.co.deepple.payment.command.domain.heartpurchaseoption;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.payment.command.domain.PaymentErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceTest {
    @Test
    @DisplayName("price 값이 1보다 작으면 예외가 발생한다.")
    void throwsExceptionWhenPriceIsLessThanOne() {
        // given
        Long value = 0L;

        // when, then
        assertThatThrownBy(() -> Price.from(value))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.INVALID_HEART_PURCHASE_OPTION);
    }

    @Test
    @DisplayName("price 값이 null이면 예외가 발생한다.")
    void throwsExceptionWhenPriceIsNull() {
        // given
        Long value = null;

        // when, then
        assertThatThrownBy(() -> Price.from(value))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("price 값이 0보다 크면 객체가 생성된다.")
    void createObjectWhenPriceIsGreaterThanZero() {
        // given
        Long value = 1L;

        // when
        Price price = Price.from(value);

        // then
        assertThat(price.getValue()).isEqualTo(value);
    }
}