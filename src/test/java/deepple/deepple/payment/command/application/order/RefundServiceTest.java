package deepple.deepple.payment.command.application.order;

import deepple.deepple.common.event.Events;
import deepple.deepple.payment.command.application.order.exception.HeartPurchaseOptionNotFoundException;
import deepple.deepple.payment.command.application.order.exception.OrderNotFoundException;
import deepple.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOption;
import deepple.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOptionCommandRepository;
import deepple.deepple.payment.command.domain.order.Order;
import deepple.deepple.payment.command.domain.order.OrderCommandRepository;
import deepple.deepple.payment.command.domain.order.PaymentMethod;
import deepple.deepple.payment.command.domain.order.event.HeartRefundedEvent;
import deepple.deepple.payment.command.domain.refund.NotificationType;
import deepple.deepple.payment.command.domain.refund.Refund;
import deepple.deepple.payment.command.domain.refund.RefundCommandRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock
    private OrderCommandRepository orderCommandRepository;

    @Mock
    private RefundCommandRepository refundCommandRepository;

    @Mock
    private HeartPurchaseOptionCommandRepository heartPurchaseOptionCommandRepository;

    @InjectMocks
    private RefundService refundService;

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

            Order order = Order.of(memberId, transactionId, paymentMethod);
            HeartPurchaseOption heartPurchaseOption = mock(HeartPurchaseOption.class);

            when(refundCommandRepository.existsByTransactionId(transactionId)).thenReturn(false);
            when(orderCommandRepository.findByTransactionIdAndPaymentMethod(transactionId, paymentMethod))
                .thenReturn(Optional.of(order));
            when(heartPurchaseOptionCommandRepository.findByProductIdAndDeletedAtIsNull(productId))
                .thenReturn(Optional.of(heartPurchaseOption));
            when(heartPurchaseOption.getHeartAmount()).thenReturn(heartAmount);

            // when & then
            try (MockedStatic<Events> eventsMockedStatic = mockStatic(Events.class)) {
                refundService.processRefund(transactionId, productId, quantity, paymentMethod, notificationType);

                verify(refundCommandRepository).existsByTransactionId(transactionId);
                verify(orderCommandRepository).findByTransactionIdAndPaymentMethod(transactionId, paymentMethod);
                verify(heartPurchaseOptionCommandRepository).findByProductIdAndDeletedAtIsNull(productId);

                ArgumentCaptor<Refund> refundCaptor = ArgumentCaptor.forClass(Refund.class);
                verify(refundCommandRepository).save(refundCaptor.capture());

                Refund savedRefund = refundCaptor.getValue();
                assertThat(savedRefund.getMemberId()).isEqualTo(memberId);
                assertThat(savedRefund.getTransactionId()).isEqualTo(transactionId);
                assertThat(savedRefund.getProductId()).isEqualTo(productId);
                assertThat(savedRefund.getQuantity()).isEqualTo(quantity);
                assertThat(savedRefund.getRefundAmount()).isEqualTo(heartAmount * quantity);
                assertThat(savedRefund.getNotificationType()).isEqualTo(notificationType);

                eventsMockedStatic.verify(() -> Events.raise(argThat(
                    event -> event instanceof HeartRefundedEvent
                        && ((HeartRefundedEvent) event).getMemberId().equals(memberId)
                        && ((HeartRefundedEvent) event).getAmount().equals(heartAmount * quantity)
                        && ((HeartRefundedEvent) event).getTransactionId().equals(transactionId)
                )));
            }
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

            Order order = Order.of(memberId, transactionId, paymentMethod);
            HeartPurchaseOption heartPurchaseOption = mock(HeartPurchaseOption.class);

            when(refundCommandRepository.existsByTransactionId(transactionId)).thenReturn(false);
            when(orderCommandRepository.findByTransactionIdAndPaymentMethod(transactionId, paymentMethod))
                .thenReturn(Optional.of(order));
            when(heartPurchaseOptionCommandRepository.findByProductIdAndDeletedAtIsNull(productId))
                .thenReturn(Optional.of(heartPurchaseOption));
            when(heartPurchaseOption.getHeartAmount()).thenReturn(heartAmount);

            // when
            refundService.processRefund(transactionId, productId, quantity, paymentMethod, notificationType);

            // then
            ArgumentCaptor<Refund> refundCaptor = ArgumentCaptor.forClass(Refund.class);
            verify(refundCommandRepository).save(refundCaptor.capture());

            Refund savedRefund = refundCaptor.getValue();
            assertThat(savedRefund.getRefundAmount()).isEqualTo(150L); // 30 * 5
        }
    }

    @Nested
    @DisplayName("중복 환불 처리 시")
    class DuplicateRefundTests {

        @Test
        @DisplayName("이미 환불된 거래인 경우 처리 중단")
        void shouldSkipWhenRefundAlreadyExists() {
            // given
            String transactionId = "txn123";
            String productId = "product123";
            int quantity = 1;
            PaymentMethod paymentMethod = PaymentMethod.APP_STORE;
            NotificationType notificationType = NotificationType.REFUND;

            when(refundCommandRepository.existsByTransactionId(transactionId)).thenReturn(true);

            // when
            refundService.processRefund(transactionId, productId, quantity, paymentMethod, notificationType);

            // then
            verify(refundCommandRepository).existsByTransactionId(transactionId);
            verify(orderCommandRepository, never()).findByTransactionIdAndPaymentMethod(any(), any());
            verify(heartPurchaseOptionCommandRepository, never()).findByProductIdAndDeletedAtIsNull(any());
            verify(refundCommandRepository, never()).save(any(Refund.class));
        }
    }

    @Nested
    @DisplayName("주문을 찾을 수 없는 경우")
    class OrderNotFoundTests {

        @Test
        @DisplayName("transactionId와 paymentMethod로 주문을 찾을 수 없으면 OrderNotFoundException 발생")
        void shouldThrowOrderNotFoundExceptionWhenOrderNotFound() {
            // given
            String transactionId = "txn123";
            String productId = "product123";
            int quantity = 1;
            PaymentMethod paymentMethod = PaymentMethod.APP_STORE;
            NotificationType notificationType = NotificationType.REFUND;

            when(refundCommandRepository.existsByTransactionId(transactionId)).thenReturn(false);
            when(orderCommandRepository.findByTransactionIdAndPaymentMethod(transactionId, paymentMethod))
                .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                () -> refundService.processRefund(transactionId, productId, quantity, paymentMethod, notificationType))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("주문을 찾을 수 없습니다");

            verify(heartPurchaseOptionCommandRepository, never()).findByProductIdAndDeletedAtIsNull(any());
            verify(refundCommandRepository, never()).save(any(Refund.class));
        }
    }

    @Nested
    @DisplayName("하트 구매 옵션을 찾을 수 없는 경우")
    class HeartPurchaseOptionNotFoundTests {

        @Test
        @DisplayName("productId로 하트 구매 옵션을 찾을 수 없으면 HeartPurchaseOptionNotFoundException 발생")
        void shouldThrowHeartPurchaseOptionNotFoundExceptionWhenOptionNotFound() {
            // given
            String transactionId = "txn123";
            String productId = "product123";
            int quantity = 1;
            PaymentMethod paymentMethod = PaymentMethod.APP_STORE;
            NotificationType notificationType = NotificationType.REFUND;
            Long memberId = 100L;

            Order order = Order.of(memberId, transactionId, paymentMethod);

            when(refundCommandRepository.existsByTransactionId(transactionId)).thenReturn(false);
            when(orderCommandRepository.findByTransactionIdAndPaymentMethod(transactionId, paymentMethod))
                .thenReturn(Optional.of(order));
            when(heartPurchaseOptionCommandRepository.findByProductIdAndDeletedAtIsNull(productId))
                .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                () -> refundService.processRefund(transactionId, productId, quantity, paymentMethod, notificationType))
                .isInstanceOf(HeartPurchaseOptionNotFoundException.class)
                .hasMessageContaining("하트 구매 옵션이 존재하지 않습니다");

            verify(refundCommandRepository, never()).save(any(Refund.class));
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

            Order order = Order.of(memberId, transactionId, paymentMethod);
            HeartPurchaseOption heartPurchaseOption = mock(HeartPurchaseOption.class);

            when(refundCommandRepository.existsByTransactionId(transactionId)).thenReturn(false);
            when(orderCommandRepository.findByTransactionIdAndPaymentMethod(transactionId, paymentMethod))
                .thenReturn(Optional.of(order));
            when(heartPurchaseOptionCommandRepository.findByProductIdAndDeletedAtIsNull(productId))
                .thenReturn(Optional.of(heartPurchaseOption));
            when(heartPurchaseOption.getHeartAmount()).thenReturn(heartAmount);

            // when
            refundService.processRefund(transactionId, productId, quantity, paymentMethod, notificationType);

            // then
            ArgumentCaptor<Refund> refundCaptor = ArgumentCaptor.forClass(Refund.class);
            verify(refundCommandRepository).save(refundCaptor.capture());

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

            Order order = spy(Order.of(memberId, transactionId, paymentMethod));
            HeartPurchaseOption heartPurchaseOption = mock(HeartPurchaseOption.class);

            when(refundCommandRepository.existsByTransactionId(transactionId)).thenReturn(false);
            when(orderCommandRepository.findByTransactionIdAndPaymentMethod(transactionId, paymentMethod))
                .thenReturn(Optional.of(order));
            when(heartPurchaseOptionCommandRepository.findByProductIdAndDeletedAtIsNull(productId))
                .thenReturn(Optional.of(heartPurchaseOption));
            when(heartPurchaseOption.getHeartAmount()).thenReturn(heartAmount);

            // when
            refundService.processRefund(transactionId, productId, quantity, paymentMethod, notificationType);

            // then
            verify(order).refund();
        }
    }
}
