package deepple.deepple.payment.command.application.order;

import deepple.deepple.payment.command.application.order.exception.HeartPurchaseOptionNotFoundException;
import deepple.deepple.payment.command.application.order.exception.OrderAlreadyExistsException;
import deepple.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOption;
import deepple.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOptionCommandRepository;
import deepple.deepple.payment.command.domain.order.Order;
import deepple.deepple.payment.command.domain.order.OrderCommandRepository;
import deepple.deepple.payment.command.domain.order.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    final long memberId = 1L;
    final String transactionId = "transactionId";
    final String productId = "productId";
    final String normalizedProductId = productId.toUpperCase();
    final int quantity = 1;
    final PaymentMethod paymentMethod = PaymentMethod.APP_STORE;

    @InjectMocks
    private OrderService orderService;
    @Mock
    private OrderCommandRepository orderCommandRepository;
    @Mock
    private HeartPurchaseOptionCommandRepository heartPurchaseOptionCommandRepository;

    @Test
    @DisplayName("transactionId가 이미 존재하는 경우 예외를 던진다.")
    void throwExceptionWhenTransactionIdAlreadyExists() {
        // given
        when(orderCommandRepository.existsByTransactionIdAndPaymentMethod(transactionId, paymentMethod)).thenReturn(
            true);

        // when & then
        assertThatThrownBy(
            () -> orderService.processReceipt(memberId, transactionId, productId, quantity, paymentMethod))
            .isInstanceOf(OrderAlreadyExistsException.class);
        verify(heartPurchaseOptionCommandRepository, never()).findByProductIdAndDeletedAtIsNull(any());
        verify(orderCommandRepository, never()).save(any());
    }

    @Test
    @DisplayName("하트 구매 옵션이 존재하지 않는 경우 예외를 던진다.")
    void throwExceptionWhenHeartPurchaseOptionNotFound() {
        // given
        when(orderCommandRepository.existsByTransactionIdAndPaymentMethod(transactionId, paymentMethod)).thenReturn(
            false);
        when(heartPurchaseOptionCommandRepository.findByProductIdAndDeletedAtIsNull(normalizedProductId)).thenReturn(
            Optional.empty());

        // when & then
        assertThatThrownBy(
            () -> orderService.processReceipt(memberId, transactionId, productId, quantity, paymentMethod))
            .isInstanceOf(HeartPurchaseOptionNotFoundException.class);
        verify(orderCommandRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("하트 구매 옵션이 존재하는 경우 주문을 생성한다.")
    void createOrderWhenHeartPurchaseOptionExists() {
        // given
        when(orderCommandRepository.existsByTransactionIdAndPaymentMethod(transactionId, paymentMethod)).thenReturn(
            false);
        HeartPurchaseOption heartPurchaseOption = mock(HeartPurchaseOption.class);

        when(heartPurchaseOptionCommandRepository.findByProductIdAndDeletedAtIsNull(normalizedProductId)).thenReturn(
            Optional.of(heartPurchaseOption));

        // when
        orderService.processReceipt(memberId, transactionId, productId, quantity, paymentMethod);

        // then
        verify(orderCommandRepository).save(argThat(
            order -> order.getMemberId() == memberId
                && order.getTransactionId().equals(transactionId)
                && order.getPaymentMethod() == paymentMethod
        ));
        verify(orderCommandRepository).save(any(Order.class));
        verify(heartPurchaseOption).purchase(memberId, quantity);
    }
}