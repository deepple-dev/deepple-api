package kr.co.deepple.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.payment.application.port.out.HeartPurchaseOptionRepository;
import kr.co.deepple.payment.application.port.out.OrderRepository;
import kr.co.deepple.payment.application.port.out.RefundRepository;
import kr.co.deepple.payment.domain.HeartPurchaseOption;
import kr.co.deepple.payment.domain.NotificationType;
import kr.co.deepple.payment.domain.Order;
import kr.co.deepple.payment.domain.OrderStatus;
import kr.co.deepple.payment.domain.PaymentErrorCode;
import kr.co.deepple.payment.domain.PaymentMethod;
import kr.co.deepple.payment.domain.Refund;
import kr.co.deepple.payment.event.HeartRefundedEvent;
import kr.co.deepple.payment.fixture.HeartPurchaseOptionFixture;
import kr.co.deepple.payment.fixture.OrderFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

  @Mock private OrderRepository orderRepository;

  @Mock private RefundRepository refundRepository;

  @Mock private HeartPurchaseOptionRepository heartPurchaseOptionRepository;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private RefundService refundService;

  @Nested
  @DisplayName("환불 처리 시")
  class ProcessRefundTests {

    @Test
    @DisplayName("정상적인 환불 처리 시 Refund 저장 및 이벤트 발행")
    void shouldProcessRefundSuccessfully() {
      // given
      String transactionId = "txn123";
      String productId = "product123";
      int quantity = 2;
      PaymentMethod paymentMethod = PaymentMethod.APP_STORE;
      NotificationType notificationType = NotificationType.REFUND;
      Long memberId = 100L;
      Long heartAmount = 50L;
      Long orderId = 1L;

      Order order =
          OrderFixture.builder()
              .id(orderId)
              .memberId(memberId)
              .transactionId(transactionId)
              .paymentMethod(paymentMethod)
              .build();
      HeartPurchaseOption heartPurchaseOption =
          HeartPurchaseOptionFixture.builder().productId(productId).amount(heartAmount).build();

      when(orderRepository.findByTransactionIdAndPaymentMethodForUpdate(
              transactionId, paymentMethod))
          .thenReturn(Optional.of(order));
      when(heartPurchaseOptionRepository.findByProductIdAndDeletedAtIsNull(productId))
          .thenReturn(Optional.of(heartPurchaseOption));

      // when & then
      refundService.processRefund(
          transactionId, productId, quantity, paymentMethod, notificationType);

      verify(orderRepository)
          .findByTransactionIdAndPaymentMethodForUpdate(transactionId, paymentMethod);
      verify(heartPurchaseOptionRepository).findByProductIdAndDeletedAtIsNull(productId);

      ArgumentCaptor<Refund> refundCaptor = ArgumentCaptor.forClass(Refund.class);
      verify(refundRepository).save(refundCaptor.capture());

      Refund savedRefund = refundCaptor.getValue();
      assertThat(savedRefund.getMemberId()).isEqualTo(memberId);
      assertThat(savedRefund.getTransactionId()).isEqualTo(transactionId);
      assertThat(savedRefund.getProductId()).isEqualTo(productId);
      assertThat(savedRefund.getQuantity()).isEqualTo(quantity);
      assertThat(savedRefund.getRefundAmount()).isEqualTo(heartAmount * quantity);
      assertThat(savedRefund.getNotificationType()).isEqualTo(notificationType);

      Mockito.verify(eventPublisher)
          .publishEvent(
              ArgumentMatchers.<Object>argThat(event ->
                  event instanceof HeartRefundedEvent
                      && ((HeartRefundedEvent) event).memberId() == memberId
                      && ((HeartRefundedEvent) event).orderId() == orderId
                      && ((HeartRefundedEvent) event).amount() == heartAmount * quantity
                      && ((HeartRefundedEvent) event).transactionId().equals(transactionId)));
    }

    @Test
    @DisplayName("quantity가 여러 개일 때 환불 금액 올바르게 계산")
    void shouldCalculateRefundAmountCorrectlyWithMultipleQuantity() {
      // given
      String transactionId = "txn123";
      String productId = "product123";
      int quantity = 5;
      PaymentMethod paymentMethod = PaymentMethod.APP_STORE;
      NotificationType notificationType = NotificationType.REFUND;
      Long memberId = 100L;
      Long heartAmount = 30L;
      Long orderId = 1L;

      Order order =
          OrderFixture.builder()
              .id(orderId)
              .memberId(memberId)
              .transactionId(transactionId)
              .paymentMethod(paymentMethod)
              .build();
      HeartPurchaseOption heartPurchaseOption =
          HeartPurchaseOptionFixture.builder().productId(productId).amount(heartAmount).build();

      when(orderRepository.findByTransactionIdAndPaymentMethodForUpdate(
              transactionId, paymentMethod))
          .thenReturn(Optional.of(order));
      when(heartPurchaseOptionRepository.findByProductIdAndDeletedAtIsNull(productId))
          .thenReturn(Optional.of(heartPurchaseOption));

      // when & then
      refundService.processRefund(
          transactionId, productId, quantity, paymentMethod, notificationType);

      ArgumentCaptor<Refund> refundCaptor = ArgumentCaptor.forClass(Refund.class);
      verify(refundRepository).save(refundCaptor.capture());

      Refund savedRefund = refundCaptor.getValue();
      assertThat(savedRefund.getRefundAmount()).isEqualTo(150L); // 30 * 5
    }
  }

  @Nested
  @DisplayName("중복 환불 처리 시")
  class DuplicateRefundTests {

    @Test
    @DisplayName("이미 환불된 주문이면 아무것도 하지 않고 정상 종료")
    void shouldSkipWhenOrderAlreadyRefunded() {
      // given
      String transactionId = "txn123";
      String productId = "product123";
      int quantity = 1;
      PaymentMethod paymentMethod = PaymentMethod.APP_STORE;
      NotificationType notificationType = NotificationType.REFUND;

      Order order =
          OrderFixture.builder()
              .transactionId(transactionId)
              .paymentMethod(paymentMethod)
              .status(OrderStatus.REFUNDED)
              .build();
      when(orderRepository.findByTransactionIdAndPaymentMethodForUpdate(
              transactionId, paymentMethod))
          .thenReturn(Optional.of(order));

      // when & then
      assertThatCode(() ->
              refundService.processRefund(
                  transactionId, productId, quantity, paymentMethod, notificationType))
          .doesNotThrowAnyException();

      verify(heartPurchaseOptionRepository, never()).findByProductIdAndDeletedAtIsNull(any());
      verify(refundRepository, never()).save(any(Refund.class));
      Mockito.verifyNoInteractions(eventPublisher);
    }
  }

  @Nested
  @DisplayName("주문을 찾을 수 없는 경우")
  class OrderNotFoundTests {

    @Test
    @DisplayName("transactionId와 paymentMethod로 주문을 찾을 수 없으면 ORDER_NOT_FOUND 예외발생")
    void shouldThrowOrderNotFoundWhenOrderNotFound() {
      // given
      String transactionId = "txn123";
      String productId = "product123";
      int quantity = 1;
      PaymentMethod paymentMethod = PaymentMethod.APP_STORE;
      NotificationType notificationType = NotificationType.REFUND;

      when(orderRepository.findByTransactionIdAndPaymentMethodForUpdate(
              transactionId, paymentMethod))
          .thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() ->
              refundService.processRefund(
                  transactionId, productId, quantity, paymentMethod, notificationType))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.ORDER_NOT_FOUND)
          .hasMessageContaining("주문을 찾을 수 없습니다");

      verify(heartPurchaseOptionRepository, never()).findByProductIdAndDeletedAtIsNull(any());
      verify(refundRepository, never()).save(any(Refund.class));
    }
  }

  @Nested
  @DisplayName("하트 구매 옵션을 찾을 수 없는 경우")
  class HeartPurchaseOptionNotFoundTests {

    @Test
    @DisplayName("productId로 하트 구매 옵션을 찾을 수 없으면 HEART_PURCHASE_OPTION_NOT_FOUND 예외발생")
    void shouldThrowHeartPurchaseOptionNotFoundWhenOptionNotFound() {
      // given
      String transactionId = "txn123";
      String productId = "product123";
      int quantity = 1;
      PaymentMethod paymentMethod = PaymentMethod.APP_STORE;
      NotificationType notificationType = NotificationType.REFUND;
      Long memberId = 100L;

      Order order =
          OrderFixture.builder()
              .memberId(memberId)
              .transactionId(transactionId)
              .paymentMethod(paymentMethod)
              .build();

      when(orderRepository.findByTransactionIdAndPaymentMethodForUpdate(
              transactionId, paymentMethod))
          .thenReturn(Optional.of(order));
      when(heartPurchaseOptionRepository.findByProductIdAndDeletedAtIsNull(productId))
          .thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() ->
              refundService.processRefund(
                  transactionId, productId, quantity, paymentMethod, notificationType))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", PaymentErrorCode.HEART_PURCHASE_OPTION_NOT_FOUND)
          .hasMessageContaining("하트 구매 옵션이 존재하지 않습니다");

      verify(refundRepository, never()).save(any(Refund.class));
    }
  }

  @Nested
  @DisplayName("다양한 NotificationType 처리 테스트")
  class NotificationTypeHandlingTests {

    @Test
    @DisplayName("REFUND_DECLINED NotificationType으로 환불 처리")
    void shouldProcessRefundWithRefundDeclinedNotificationType() {
      // given
      String transactionId = "txn123";
      String productId = "product123";
      int quantity = 1;
      PaymentMethod paymentMethod = PaymentMethod.APP_STORE;
      NotificationType notificationType = NotificationType.REFUND_DECLINED;
      Long memberId = 100L;
      Long heartAmount = 50L;
      Long orderId = 1L;

      Order order =
          OrderFixture.builder()
              .id(orderId)
              .memberId(memberId)
              .transactionId(transactionId)
              .paymentMethod(paymentMethod)
              .build();
      HeartPurchaseOption heartPurchaseOption =
          HeartPurchaseOptionFixture.builder().productId(productId).amount(heartAmount).build();

      when(orderRepository.findByTransactionIdAndPaymentMethodForUpdate(
              transactionId, paymentMethod))
          .thenReturn(Optional.of(order));
      when(heartPurchaseOptionRepository.findByProductIdAndDeletedAtIsNull(productId))
          .thenReturn(Optional.of(heartPurchaseOption));

      // when & then
      refundService.processRefund(
          transactionId, productId, quantity, paymentMethod, notificationType);

      ArgumentCaptor<Refund> refundCaptor = ArgumentCaptor.forClass(Refund.class);
      verify(refundRepository).save(refundCaptor.capture());

      Refund savedRefund = refundCaptor.getValue();
      assertThat(savedRefund.getNotificationType()).isEqualTo(NotificationType.REFUND_DECLINED);
    }
  }

  @Nested
  @DisplayName("Order 상태 변경 테스트")
  class OrderStatusChangeTests {

    @Test
    @DisplayName("환불 처리 시 Order의 refund() 메서드 호출")
    void shouldCallOrderRefundMethod() {
      // given
      String transactionId = "txn123";
      String productId = "product123";
      int quantity = 1;
      PaymentMethod paymentMethod = PaymentMethod.APP_STORE;
      NotificationType notificationType = NotificationType.REFUND;
      Long memberId = 100L;
      Long heartAmount = 50L;
      Long orderId = 1L;

      Order order =
          spy(
              OrderFixture.builder()
                  .id(orderId)
                  .memberId(memberId)
                  .transactionId(transactionId)
                  .paymentMethod(paymentMethod)
                  .build());
      HeartPurchaseOption heartPurchaseOption =
          HeartPurchaseOptionFixture.builder().productId(productId).amount(heartAmount).build();

      when(orderRepository.findByTransactionIdAndPaymentMethodForUpdate(
              transactionId, paymentMethod))
          .thenReturn(Optional.of(order));
      when(heartPurchaseOptionRepository.findByProductIdAndDeletedAtIsNull(productId))
          .thenReturn(Optional.of(heartPurchaseOption));

      // when & then
      refundService.processRefund(
          transactionId, productId, quantity, paymentMethod, notificationType);

      verify(order).refund();
    }
  }
}
