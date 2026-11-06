package deepple.deepple.payment.command.domain.order;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.payment.command.domain.PaymentErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {
    @Nested
    @DisplayName("Order 객체 생성 테스트")
    class CreateOrderTest {
        @ParameterizedTest
        @ValueSource(strings = {"memberId is null", "transactionId is null", "paymentMethod is null"})
        @DisplayName("Order의 파라미터가 Null이면 NullPointerException 발생")
        void throwsNullPointerExceptionWhenParameterIsNull(String condition) {
            // given
            Long memberId = condition.equals("memberId is null") ? null : 1L;
            String transactionId = condition.equals("transactionId is null") ? null : "transactionId";
            PaymentMethod paymentMethod = condition.equals("paymentMethod is null") ? null : PaymentMethod.GOOGLE_PLAY;
            // when & then
            assertThatThrownBy(() -> Order.of(memberId, transactionId, paymentMethod))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Order의 파라미터가 Null이 아니면 Order 객체 PAID 상태로 생성")
        void createOrderSuccessWhenParameterIsNotNull() {
            // given
            Long memberId = 1L;
            String transactionId = "transactionId";
            PaymentMethod paymentMethod = PaymentMethod.GOOGLE_PLAY;
            // when
            Order order = Order.of(memberId, transactionId, paymentMethod);
            // then
            assertThat(order).isNotNull();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        }
    }

    @Nested
    @DisplayName("Order 객체의 상태가 PAID일 때")
    class OrderStatusPaid {
        Order order;

        @BeforeEach
        void setUp() {
            // given
            order = Order.of(1L, "transactionId", PaymentMethod.GOOGLE_PLAY);
        }

        @Test
        @DisplayName("refund() 메서드 호출 시 REFUNDED 상태로 변경")
        void changeStatusToRefundedWhenRefundMethodIsCalled() {
            // when
            order.refund();
            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUNDED);
        }
    }

    @Nested
    @DisplayName("Order 객체의 상태가 REFUNDED일 때")
    class OrderStatusRefunded {
        Order order;

        @BeforeEach
        void setUp() {
            // given
            order = Order.of(1L, "transactionId", PaymentMethod.GOOGLE_PLAY);
            order.refund();
        }

        @Test
        @DisplayName("refund() 메서드 호출 시 ORDER_NOT_REFUNDABLE 예외발생")
        void throwsOrderNotRefundableWhenRefundMethodIsCalled() {
            // when & then
            assertThatThrownBy(order::refund)
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.ORDER_NOT_REFUNDABLE);
        }
    }
}