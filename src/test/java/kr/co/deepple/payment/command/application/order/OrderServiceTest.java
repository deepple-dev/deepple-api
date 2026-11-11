package kr.co.deepple.payment.command.application.order;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.lock.DistributedLockTemplate;
import kr.co.deepple.payment.command.domain.PaymentErrorCode;
import kr.co.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOption;
import kr.co.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOptionCommandRepository;
import kr.co.deepple.payment.command.domain.order.Order;
import kr.co.deepple.payment.command.domain.order.OrderCommandRepository;
import kr.co.deepple.payment.command.domain.order.PaymentMethod;
import kr.co.deepple.payment.fixture.OrderFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    final long memberId = 1L;
    final long orderId = 10L;
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
    @Mock
    private DistributedLockTemplate distributedLockTemplate;

    @BeforeEach
    void setUp() {
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(1).run();
            return null;
        }).when(distributedLockTemplate).executeInTransaction(anyString(), any(Runnable.class));
    }

    @Test
    @DisplayName("거래 ID와 결제 수단으로 락 키를 만들어 락 안에서 처리한다.")
    void processInLockKeyedByTransaction() {
        // given
        when(orderCommandRepository.findByTransactionIdAndPaymentMethod(transactionId, paymentMethod))
            .thenReturn(Optional.of(OrderFixture.builder().memberId(memberId).build()));

        // when
        orderService.processReceipt(memberId, transactionId, productId, quantity, paymentMethod);

        // then
        verify(distributedLockTemplate).executeInTransaction(eq("order:APP_STORE:transactionId"), any(Runnable.class));
    }

    @Test
    @DisplayName("같은 회원의 주문이 이미 있으면 하트를 다시 지급하지 않고 정상 종료한다.")
    void succeedWithoutPurchaseWhenSameMemberOrderExists() {
        // given
        when(orderCommandRepository.findByTransactionIdAndPaymentMethod(transactionId, paymentMethod))
            .thenReturn(Optional.of(OrderFixture.builder().memberId(memberId).build()));

        // when & then
        assertThatCode(() -> orderService.processReceipt(memberId, transactionId, productId, quantity, paymentMethod))
            .doesNotThrowAnyException();
        verify(heartPurchaseOptionCommandRepository, never()).findByProductIdAndDeletedAtIsNull(any());
        verify(orderCommandRepository, never()).save(any());
    }

    @Test
    @DisplayName("다른 회원의 주문이 이미 있으면 ORDER_ALREADY_EXISTS 예외를 던진다.")
    void throwExceptionWhenOtherMemberOrderExists() {
        // given
        when(orderCommandRepository.findByTransactionIdAndPaymentMethod(transactionId, paymentMethod))
            .thenReturn(Optional.of(OrderFixture.builder().memberId(memberId + 1).build()));

        // when & then
        assertThatThrownBy(
            () -> orderService.processReceipt(memberId, transactionId, productId, quantity, paymentMethod))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.ORDER_ALREADY_EXISTS);
        verify(orderCommandRepository, never()).save(any());
    }

    @Test
    @DisplayName("하트 구매 옵션이 존재하지 않는 경우 예외를 던진다.")
    void throwExceptionWhenHeartPurchaseOptionNotFound() {
        // given
        when(orderCommandRepository.findByTransactionIdAndPaymentMethod(transactionId, paymentMethod))
            .thenReturn(Optional.empty());
        when(orderCommandRepository.save(any(Order.class))).thenReturn(OrderFixture.builder().id(orderId).build());
        when(heartPurchaseOptionCommandRepository.findByProductIdAndDeletedAtIsNull(normalizedProductId)).thenReturn(
            Optional.empty());

        // when & then
        assertThatThrownBy(
            () -> orderService.processReceipt(memberId, transactionId, productId, quantity, paymentMethod))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.HEART_PURCHASE_OPTION_NOT_FOUND);
    }

    @Test
    @DisplayName("주문이 없으면 주문을 생성하고 주문 ID로 하트를 지급한다.")
    void createOrderAndPurchaseWhenOrderNotExists() {
        // given
        when(orderCommandRepository.findByTransactionIdAndPaymentMethod(transactionId, paymentMethod))
            .thenReturn(Optional.empty());
        when(orderCommandRepository.save(any(Order.class))).thenReturn(OrderFixture.builder().id(orderId).build());
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
        verify(heartPurchaseOption).purchase(memberId, orderId, quantity);
    }
}
