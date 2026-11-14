package kr.co.deepple.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.heart.application.port.out.HeartTransactionRepository;
import kr.co.deepple.heart.application.port.out.MemberHeartBalanceRepository;
import kr.co.deepple.heart.fixture.MemberHeartBalanceFixture;
import kr.co.deepple.member.application.port.out.MemberRepository;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.payment.application.port.out.HeartPurchaseOptionRepository;
import kr.co.deepple.payment.application.port.out.OrderRepository;
import kr.co.deepple.payment.domain.HeartPurchaseOption;
import kr.co.deepple.payment.domain.PaymentErrorCode;
import kr.co.deepple.payment.domain.PaymentMethod;
import kr.co.deepple.payment.fixture.HeartPurchaseOptionFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("결제 영수증 처리 동시성 통합 테스트")
class OrderServiceConcurrencyIntegrationTest extends IntegrationTestSupport {

  private static final int THREAD_COUNT = 20;
  private static final long INITIAL_BALANCE = 100L;
  private static final long OPTION_AMOUNT = 45L;
  private static final String TRANSACTION_ID = "duplicate-receipt-transaction";

  @Autowired private OrderService orderService;

  @Autowired private OrderRepository orderRepository;

  @Autowired private HeartPurchaseOptionRepository heartPurchaseOptionRepository;

  @Autowired private MemberRepository memberRepository;

  @Autowired private MemberHeartBalanceRepository memberHeartBalanceRepository;

  @Autowired private HeartTransactionRepository heartTransactionRepository;

  private HeartPurchaseOption option;
  private Member member;

  @BeforeEach
  void setUp() {
    member = memberRepository.save(MemberFixture.builder().build());
    saveHeartBalance(member.getId());
    option =
        heartPurchaseOptionRepository.save(
            HeartPurchaseOptionFixture.builder().amount(OPTION_AMOUNT).build());
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
  @DisplayName("같은 회원이 같은 영수증을 다시 보내면 성공하고 하트는 한 번만 지급된다")
  void succeedWithoutRegrantWhenSameMemberRetries() {
    // given
    processReceipt(member.getId());

    // when & then
    assertThatCode(() -> processReceipt(member.getId())).doesNotThrowAnyException();
    assertThat(orderRepository.count()).isEqualTo(1);
    assertGrantedOnce();
  }

  @Test
  @DisplayName("다른 회원이 같은 영수증을 보내면 ORDER_ALREADY_EXISTS 예외를 던진다")
  void throwWhenOtherMemberSendsSameReceipt() {
    // given
    processReceipt(member.getId());
    Member otherMember = memberRepository.save(MemberFixture.builder().build());

    // when & then
    assertThatThrownBy(() -> processReceipt(otherMember.getId()))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.ORDER_ALREADY_EXISTS);
    assertGrantedOnce();
  }

  @Test
  @DisplayName("같은 회원이 같은 영수증을 동시에 보내도 모두 성공하고 주문은 하나, 하트는 한 번만 지급된다")
  void succeedAllAndGrantOnceWhenSameReceiptSentConcurrently() throws InterruptedException {
    // given
    long memberId = member.getId();

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
            processReceipt(memberId);
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
    assertThat(orderRepository.count()).isEqualTo(1);
    assertGrantedOnce();
  }

  private void processReceipt(long memberId) {
    orderService.processReceipt(
        memberId, TRANSACTION_ID, option.getProductId(), 1, PaymentMethod.GOOGLE_PLAY);
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
