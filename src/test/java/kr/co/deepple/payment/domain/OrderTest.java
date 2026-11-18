package kr.co.deepple.payment.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.payment.fixture.OrderFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OrderTest {

  @Nested
  @DisplayName("Order 객체 생성 테스트")
  class CreateOrderTest {

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
      order = OrderFixture.builder().build();
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
      order = OrderFixture.builder().status(OrderStatus.REFUNDED).build();
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
