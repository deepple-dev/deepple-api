package kr.co.deepple.heart.adapter.in.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.heart.application.port.out.HeartTransactionRepository;
import kr.co.deepple.heart.application.port.out.MemberHeartBalanceRepository;
import kr.co.deepple.heart.domain.HeartTransaction;
import kr.co.deepple.heart.domain.TransactionType;
import kr.co.deepple.heart.fixture.MemberHeartBalanceFixture;
import kr.co.deepple.payment.event.HeartPurchasedEvent;
import kr.co.deepple.payment.event.HeartRefundedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("하트 지급·회수 이벤트 멱등성 통합 테스트")
class HeartPaymentEventHandlerIntegrationTest extends IntegrationTestSupport {

  private static final long MEMBER_ID = 1L;
  private static final long INITIAL_BALANCE = 100L;
  private static final long AMOUNT = 45L;
  private static final long ORDER_ID = 1L;

  @Autowired private HeartPaymentEventHandler heartPaymentEventHandler;

  @Autowired private MemberHeartBalanceRepository memberHeartBalanceRepository;

  @Autowired private HeartTransactionRepository heartTransactionRepository;

  @AfterEach
  void tearDown() {
    heartTransactionRepository.deleteAll();
    memberHeartBalanceRepository.deleteAll();
  }

  @Test
  @DisplayName("같은 하트 지급 이벤트를 두 번 처리해도 하트는 한 번만 지급되고 거래 내역도 하나만 남는다")
  void addOnceWhenSamePurchasedEventHandledTwice() {
    // given
    saveBalance();
    HeartPurchasedEvent event = HeartPurchasedEvent.of(MEMBER_ID, ORDER_ID, AMOUNT);

    // when
    heartPaymentEventHandler.handle(event);
    heartPaymentEventHandler.handle(event);

    // then
    assertBalanceStays(INITIAL_BALANCE + AMOUNT);
    assertSingleTransaction(TransactionType.PURCHASE, AMOUNT);
  }

  @Test
  @DisplayName("같은 하트 회수 이벤트를 두 번 처리해도 하트는 한 번만 회수되고 거래 내역도 하나만 남는다")
  void refundOnceWhenSameRefundedEventHandledTwice() {
    // given
    saveBalance();
    HeartRefundedEvent event =
        HeartRefundedEvent.of(MEMBER_ID, ORDER_ID, AMOUNT, "refund-transaction");

    // when
    heartPaymentEventHandler.handle(event);
    heartPaymentEventHandler.handle(event);

    // then
    assertBalanceStays(INITIAL_BALANCE - AMOUNT);
    assertSingleTransaction(TransactionType.REFUND, -AMOUNT);
  }

  @Test
  @DisplayName("같은 주문의 지급과 회수는 서로 다른 거래로 각각 처리된다")
  void handlePurchaseAndRefundOfSameOrderSeparately() {
    // given
    saveBalance();

    // when
    heartPaymentEventHandler.handle(HeartPurchasedEvent.of(MEMBER_ID, ORDER_ID, AMOUNT));
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> assertThat(purchaseHeartBalance()).isEqualTo(INITIAL_BALANCE + AMOUNT));
    heartPaymentEventHandler.handle(
        HeartRefundedEvent.of(MEMBER_ID, ORDER_ID, AMOUNT, "refund-transaction"));

    // then
    assertBalanceStays(INITIAL_BALANCE);
    assertThat(heartTransactionRepository.count()).isEqualTo(2L);
  }

  private void saveBalance() {
    memberHeartBalanceRepository.save(
        MemberHeartBalanceFixture.builder()
            .memberId(MEMBER_ID)
            .purchaseBalance(INITIAL_BALANCE)
            .build());
  }

  private void assertBalanceStays(long expected) {
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() -> assertThat(purchaseHeartBalance()).isEqualTo(expected));
    await()
        .during(Duration.ofSeconds(1))
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(() -> assertThat(purchaseHeartBalance()).isEqualTo(expected));
  }

  private void assertSingleTransaction(TransactionType transactionType, long amount) {
    List<HeartTransaction> transactions = heartTransactionRepository.findAll();
    assertThat(transactions).hasSize(1);
    assertThat(transactions.getFirst().getTransactionType()).isEqualTo(transactionType);
    assertThat(transactions.getFirst().getAmount()).isEqualTo(amount);
  }

  private long purchaseHeartBalance() {
    return memberHeartBalanceRepository
        .findByMemberId(MEMBER_ID)
        .orElseThrow()
        .getPurchaseBalance();
  }
}
