package kr.co.deepple.payment.command.application.order;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.services.androidpublisher.model.ProductPurchase;
import java.util.Optional;
import kr.co.deepple.common.event.Events;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.payment.command.domain.PaymentErrorCode;
import kr.co.deepple.payment.command.domain.order.OrderCommandRepository;
import kr.co.deepple.payment.command.domain.order.PaymentMethod;
import kr.co.deepple.payment.command.domain.order.event.GooglePlayReceiptVerifiedEvent;
import kr.co.deepple.payment.command.infra.order.GooglePlayClient;
import kr.co.deepple.payment.fixture.OrderFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GooglePlayPaymentServiceTest {

  private static final String PRODUCT_ID = "heart_pack_100";
  private static final String PURCHASE_TOKEN = "purchase.token.value";
  private static final String ORDER_ID = "GPA.1234-5678";
  private static final long MEMBER_ID = 1L;
  private static final int QUANTITY = 1;

  @Mock private GooglePlayClient googlePlayClient;

  @Mock private OrderCommandRepository orderCommandRepository;

  @InjectMocks private GooglePlayPaymentService googlePlayPaymentService;

  @Test
  @DisplayName("구매가 검증되면 GooglePlayReceiptVerifiedEvent를 발행하고 consume을 호출한다")
  void successWhenPurchaseVerified() {
    // given
    ProductPurchase purchase = new ProductPurchase().setOrderId(ORDER_ID);
    when(googlePlayClient.verifyPurchase(PRODUCT_ID, PURCHASE_TOKEN)).thenReturn(purchase);
    when(googlePlayClient.resolveQuantity(purchase)).thenReturn(QUANTITY);

    // when & then
    try (MockedStatic<Events> eventsMockedStatic = mockStatic(Events.class)) {
      googlePlayPaymentService.verifyReceipt(PRODUCT_ID, PURCHASE_TOKEN, MEMBER_ID);

      eventsMockedStatic.verify(() ->
          Events.raise(
              argThat(event ->
                  event instanceof GooglePlayReceiptVerifiedEvent
                      && ((GooglePlayReceiptVerifiedEvent) event).memberId() == MEMBER_ID
                      && ((GooglePlayReceiptVerifiedEvent) event).transactionId().equals(ORDER_ID)
                      && ((GooglePlayReceiptVerifiedEvent) event).productId().equals(PRODUCT_ID)
                      && ((GooglePlayReceiptVerifiedEvent) event).quantity() == QUANTITY)));
      verify(googlePlayClient).consumePurchase(PRODUCT_ID, PURCHASE_TOKEN);
    }
  }

  @Test
  @DisplayName("검증 실패 시 INVALID_GOOGLE_PLAY_RECEIPT 예외를 던지고 consume은 호출하지 않는다")
  void throwsWhenVerificationFails() {
    // given
    when(googlePlayClient.verifyPurchase(PRODUCT_ID, PURCHASE_TOKEN))
        .thenThrow(new DeeppleException(PaymentErrorCode.INVALID_GOOGLE_PLAY_RECEIPT, "invalid"));

    // when & then
    assertThatThrownBy(
            () -> googlePlayPaymentService.verifyReceipt(PRODUCT_ID, PURCHASE_TOKEN, MEMBER_ID))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.INVALID_GOOGLE_PLAY_RECEIPT);

    verify(googlePlayClient, never()).consumePurchase(PRODUCT_ID, PURCHASE_TOKEN);
  }

  @Test
  @DisplayName("consume 실패는 로깅만 하고 예외를 전파하지 않는다 (하트는 이미 지급됨)")
  void swallowsConsumeFailure() {
    // given
    ProductPurchase purchase = new ProductPurchase().setOrderId(ORDER_ID);
    when(googlePlayClient.verifyPurchase(PRODUCT_ID, PURCHASE_TOKEN)).thenReturn(purchase);
    when(googlePlayClient.resolveQuantity(purchase)).thenReturn(QUANTITY);
    doThrow(
            new DeeppleException(
                PaymentErrorCode.GOOGLE_PLAY_UNAVAILABLE,
                "consume failed",
                new RuntimeException("consume failed")))
        .when(googlePlayClient)
        .consumePurchase(PRODUCT_ID, PURCHASE_TOKEN);

    // when & then
    try (MockedStatic<Events> eventsMockedStatic = mockStatic(Events.class)) {
      assertThatCode(
              () -> googlePlayPaymentService.verifyReceipt(PRODUCT_ID, PURCHASE_TOKEN, MEMBER_ID))
          .doesNotThrowAnyException();

      eventsMockedStatic.verify(
          () -> Events.raise(argThat(event -> event instanceof GooglePlayReceiptVerifiedEvent)));
    }
  }

  @Test
  @DisplayName("이미 consume된 구매가 같은 회원의 주문이면 이벤트와 consume 없이 정상 종료한다")
  void succeedWhenConsumedPurchaseOrderedBySameMember() {
    // given
    ProductPurchase purchase = new ProductPurchase().setOrderId(ORDER_ID).setConsumptionState(1);
    when(googlePlayClient.verifyPurchase(PRODUCT_ID, PURCHASE_TOKEN)).thenReturn(purchase);
    when(orderCommandRepository.findByTransactionIdAndPaymentMethod(
            ORDER_ID, PaymentMethod.GOOGLE_PLAY))
        .thenReturn(Optional.of(OrderFixture.builder().memberId(MEMBER_ID).build()));

    // when & then
    try (MockedStatic<Events> eventsMockedStatic = mockStatic(Events.class)) {
      assertThatCode(
              () -> googlePlayPaymentService.verifyReceipt(PRODUCT_ID, PURCHASE_TOKEN, MEMBER_ID))
          .doesNotThrowAnyException();

      eventsMockedStatic.verifyNoInteractions();
      verify(googlePlayClient, never()).consumePurchase(PRODUCT_ID, PURCHASE_TOKEN);
    }
  }

  @Test
  @DisplayName("이미 consume된 구매에 같은 회원의 주문이 없으면 INVALID_GOOGLE_PLAY_RECEIPT 예외를 던진다")
  void throwsWhenConsumedPurchaseHasNoOrderOfMember() {
    // given
    ProductPurchase purchase = new ProductPurchase().setOrderId(ORDER_ID).setConsumptionState(1);
    when(googlePlayClient.verifyPurchase(PRODUCT_ID, PURCHASE_TOKEN)).thenReturn(purchase);
    when(orderCommandRepository.findByTransactionIdAndPaymentMethod(
            ORDER_ID, PaymentMethod.GOOGLE_PLAY))
        .thenReturn(Optional.of(OrderFixture.builder().memberId(MEMBER_ID + 1).build()));

    // when & then
    assertThatThrownBy(
            () -> googlePlayPaymentService.verifyReceipt(PRODUCT_ID, PURCHASE_TOKEN, MEMBER_ID))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.INVALID_GOOGLE_PLAY_RECEIPT);
    verify(googlePlayClient, never()).consumePurchase(PRODUCT_ID, PURCHASE_TOKEN);
  }
}
