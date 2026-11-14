package kr.co.deepple.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.services.androidpublisher.model.ProductPurchase;
import java.time.Duration;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.heart.application.port.out.HeartTransactionRepository;
import kr.co.deepple.heart.application.port.out.MemberHeartBalanceRepository;
import kr.co.deepple.heart.fixture.MemberHeartBalanceFixture;
import kr.co.deepple.member.application.port.out.MemberRepository;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.payment.adapter.out.store.GooglePlayClient;
import kr.co.deepple.payment.application.port.out.HeartPurchaseOptionRepository;
import kr.co.deepple.payment.application.port.out.OrderRepository;
import kr.co.deepple.payment.domain.HeartPurchaseOption;
import kr.co.deepple.payment.domain.PaymentErrorCode;
import kr.co.deepple.payment.fixture.HeartPurchaseOptionFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

@DisplayName("Google Play 결제 재시도 통합 테스트")
class GooglePlayPaymentServiceIntegrationTest extends IntegrationTestSupport {

  private static final long INITIAL_BALANCE = 100L;
  private static final long OPTION_AMOUNT = 45L;
  private static final String ORDER_ID = "GPA.retry-order";
  private static final String PURCHASE_TOKEN = "retry-purchase-token";
  private static final int CONSUMPTION_STATE_YET_TO_BE_CONSUMED = 0;
  private static final int CONSUMPTION_STATE_CONSUMED = 1;

  private final GooglePlayClient googlePlayClient = mock(GooglePlayClient.class);
  private GooglePlayPaymentService googlePlayPaymentService;

  @Autowired private OrderRepository orderRepository;

  @Autowired private HeartPurchaseOptionRepository heartPurchaseOptionRepository;

  @Autowired private MemberRepository memberRepository;

  @Autowired private MemberHeartBalanceRepository memberHeartBalanceRepository;

  @Autowired private HeartTransactionRepository heartTransactionRepository;

  @Autowired private ApplicationEventPublisher eventPublisher;

  private HeartPurchaseOption option;
  private Member member;

  @BeforeEach
  void setUp() {
    member = memberRepository.save(MemberFixture.builder().build());
    saveHeartBalance(member.getId());
    option =
        heartPurchaseOptionRepository.save(
            HeartPurchaseOptionFixture.builder().amount(OPTION_AMOUNT).build());
    when(googlePlayClient.resolveQuantity(any())).thenReturn(1);
    googlePlayPaymentService =
        new GooglePlayPaymentService(googlePlayClient, orderRepository, eventPublisher);
  }

  @AfterEach
  void tearDown() {
    memberHeartBalanceRepository.deleteAll();
    orderRepository.deleteAll();
    heartTransactionRepository.deleteAll();
    heartPurchaseOptionRepository.delete(option);
    memberRepository.deleteAll();
  }

  @Test
  @DisplayName("consume에 실패한 구매를 다시 보내면 하트는 다시 지급하지 않고 consume을 다시 시도한다")
  void retryConsumeWithoutRegrantWhenConsumeFailedBefore() {
    // given
    when(googlePlayClient.verifyPurchase(option.getProductId(), PURCHASE_TOKEN))
        .thenReturn(purchase(CONSUMPTION_STATE_YET_TO_BE_CONSUMED));
    doThrow(new DeeppleException(PaymentErrorCode.GOOGLE_PLAY_UNAVAILABLE, "unavailable"))
        .doNothing()
        .when(googlePlayClient)
        .consumePurchase(option.getProductId(), PURCHASE_TOKEN);
    verifyReceipt();

    // when & then
    assertThatCode(this::verifyReceipt).doesNotThrowAnyException();
    verify(googlePlayClient, times(2)).consumePurchase(option.getProductId(), PURCHASE_TOKEN);
    assertGrantedOnce();
  }

  @Test
  @DisplayName("이미 처리하고 consume까지 끝난 구매를 같은 회원이 다시 보내면 성공한다")
  void succeedWhenAlreadyConsumedPurchaseIsResent() {
    // given
    when(googlePlayClient.verifyPurchase(option.getProductId(), PURCHASE_TOKEN))
        .thenReturn(purchase(CONSUMPTION_STATE_YET_TO_BE_CONSUMED))
        .thenReturn(purchase(CONSUMPTION_STATE_CONSUMED));
    verifyReceipt();

    // when & then
    assertThatCode(this::verifyReceipt).doesNotThrowAnyException();
    verify(googlePlayClient, times(1)).consumePurchase(option.getProductId(), PURCHASE_TOKEN);
    assertGrantedOnce();
  }

  @Test
  @DisplayName("주문 기록 없이 이미 consume된 구매는 INVALID_GOOGLE_PLAY_RECEIPT 예외를 던진다")
  void throwWhenConsumedPurchaseHasNoOrder() {
    // given
    when(googlePlayClient.verifyPurchase(option.getProductId(), PURCHASE_TOKEN))
        .thenReturn(purchase(CONSUMPTION_STATE_CONSUMED));

    // when & then
    assertThatThrownBy(this::verifyReceipt)
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.INVALID_GOOGLE_PLAY_RECEIPT);
    assertThat(orderRepository.count()).isZero();
    verify(googlePlayClient, never()).consumePurchase(any(), any());
  }

  private void verifyReceipt() {
    googlePlayPaymentService.verifyReceipt(option.getProductId(), PURCHASE_TOKEN, member.getId());
  }

  private ProductPurchase purchase(int consumptionState) {
    return new ProductPurchase()
        .setOrderId(ORDER_ID)
        .setPurchaseState(0)
        .setConsumptionState(consumptionState)
        .setQuantity(1);
  }

  private void assertGrantedOnce() {
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> assertThat(purchaseHeartBalance()).isEqualTo(INITIAL_BALANCE + OPTION_AMOUNT));
    await()
        .during(Duration.ofSeconds(1))
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(() -> {
          assertThat(purchaseHeartBalance()).isEqualTo(INITIAL_BALANCE + OPTION_AMOUNT);
          assertThat(heartTransactionRepository.count()).isEqualTo(1);
        });
  }

  private long purchaseHeartBalance() {
    return memberHeartBalanceRepository
        .findByMemberId(member.getId())
        .orElseThrow()
        .getPurchaseBalance();
  }

  private void saveHeartBalance(long memberId) {
    memberHeartBalanceRepository.save(
        MemberHeartBalanceFixture.builder()
            .memberId(memberId)
            .purchaseBalance(INITIAL_BALANCE)
            .build());
  }
}
