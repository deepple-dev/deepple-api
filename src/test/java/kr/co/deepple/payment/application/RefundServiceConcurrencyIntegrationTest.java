package kr.co.deepple.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.heart.application.port.out.MemberHeartBalanceRepository;
import kr.co.deepple.heart.fixture.MemberHeartBalanceFixture;
import kr.co.deepple.member.application.port.out.MemberRepository;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.payment.application.port.out.HeartPurchaseOptionRepository;
import kr.co.deepple.payment.application.port.out.OrderRepository;
import kr.co.deepple.payment.application.port.out.RefundRepository;
import kr.co.deepple.payment.domain.HeartPurchaseOption;
import kr.co.deepple.payment.domain.NotificationType;
import kr.co.deepple.payment.domain.Order;
import kr.co.deepple.payment.domain.PaymentMethod;
import kr.co.deepple.payment.fixture.HeartPurchaseOptionFixture;
import kr.co.deepple.payment.fixture.OrderFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("환불 동시성 통합 테스트")
class RefundServiceConcurrencyIntegrationTest extends IntegrationTestSupport {

  private static final int THREAD_COUNT = 20;
  private static final long INITIAL_BALANCE = 100L;
  private static final long OPTION_AMOUNT = 45L;

  @Autowired private RefundService refundService;

  @Autowired private MemberRepository memberRepository;

  @Autowired private MemberHeartBalanceRepository memberHeartBalanceRepository;

  @Autowired private OrderRepository orderRepository;

  @Autowired private RefundRepository refundRepository;

  @Autowired private HeartPurchaseOptionRepository heartPurchaseOptionRepository;

  private HeartPurchaseOption option;

  @AfterEach
  void tearDown() {
    memberHeartBalanceRepository.deleteAll();
    refundRepository.deleteAll();
    orderRepository.deleteAll();
    heartPurchaseOptionRepository.delete(option);
    memberRepository.deleteAll();
  }

  @Test
  @DisplayName("같은 환불 알림이 동시에 와도 모두 정상 종료하고 환불은 하나, 하트는 한 번만 회수된다")
  void refundOnceWithoutExceptionWhenNotifiedConcurrently() throws InterruptedException {
    // given
    Member member = memberRepository.save(MemberFixture.builder().build());
    saveHeartBalance(member.getId());
    option =
        heartPurchaseOptionRepository.save(
            HeartPurchaseOptionFixture.builder().amount(OPTION_AMOUNT).build());
    Order order = orderRepository.save(OrderFixture.builder().memberId(member.getId()).build());
    String transactionId = order.getTransactionId();
    String productId = option.getProductId();

    // when
    AtomicInteger successCount = new AtomicInteger();
    AtomicInteger failureCount = new AtomicInteger();
    CountDownLatch startSignal = new CountDownLatch(1);
    CountDownLatch doneSignal = new CountDownLatch(THREAD_COUNT);

    try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT)) {
      for (int i = 0; i < THREAD_COUNT; i++) {
        executor.submit(() -> {
          try {
            startSignal.await();
            refundService.processRefund(
                transactionId, productId, 1, PaymentMethod.GOOGLE_PLAY, NotificationType.REFUND);
            successCount.incrementAndGet();
          } catch (Exception e) {
            failureCount.incrementAndGet();
          } finally {
            doneSignal.countDown();
          }
        });
      }
      startSignal.countDown();
      assertThat(doneSignal.await(30, TimeUnit.SECONDS)).isTrue();
    }

    // then
    assertThat(successCount.get()).isEqualTo(THREAD_COUNT);
    assertThat(failureCount.get()).isZero();
    assertThat(refundRepository.count()).isEqualTo(1);
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() ->
            assertThat(purchaseHeartBalance(member)).isEqualTo(INITIAL_BALANCE - OPTION_AMOUNT));
    await()
        .during(Duration.ofSeconds(1))
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(() ->
            assertThat(purchaseHeartBalance(member)).isEqualTo(INITIAL_BALANCE - OPTION_AMOUNT));
  }

  private long purchaseHeartBalance(Member member) {
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
