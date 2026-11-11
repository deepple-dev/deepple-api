package kr.co.deepple.payment.command.domain.heartpurchaseoption;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.payment.command.domain.PaymentErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeartPurchaseAmountTest {

    @Test
    @DisplayName("amount 값이 1보다 작으면 예외가 발생한다.")
    void throwsExceptionWhenAmountIsLessThanOne() {
        // given
        Long amount = 0L;

        // when, then
        assertThatThrownBy(() -> HeartPurchaseAmount.from(amount))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.INVALID_HEART_PURCHASE_OPTION);
    }

    @Test
    @DisplayName("amount 값이 null이면 예외가 발생한다.")
    void throwsExceptionWhenAmountIsNull() {
        // given
        Long amount = null;

        // when, then
        assertThatThrownBy(() -> HeartPurchaseAmount.from(amount))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("amount 값이 0보다 크면 객체가 생성된다.")
    void createObjectWhenAmountIsGreaterThanZero() {
        // given
        Long amount = 1L;

        // when
        HeartPurchaseAmount heartPurchaseAmount = HeartPurchaseAmount.from(amount);

        // then
        assertThat(heartPurchaseAmount.getAmount()).isEqualTo(amount);
    }
}