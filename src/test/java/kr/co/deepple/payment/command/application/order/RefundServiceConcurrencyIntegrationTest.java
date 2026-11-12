package kr.co.deepple.payment.command.application.order;

import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.heart.application.port.out.MemberHeartBalanceRepository;
import kr.co.deepple.heart.fixture.MemberHeartBalanceFixture;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.infra.member.MemberCommandJpaRepository;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOption;
import kr.co.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOptionCommandRepository;
import kr.co.deepple.payment.command.domain.order.Order;
import kr.co.deepple.payment.command.domain.order.OrderCommandRepository;
import kr.co.deepple.payment.command.domain.order.PaymentMethod;
import kr.co.deepple.payment.command.domain.refund.NotificationType;
import kr.co.deepple.payment.command.domain.refund.RefundCommandRepository;
import kr.co.deepple.payment.fixture.HeartPurchaseOptionFixture;
import kr.co.deepple.payment.fixture.OrderFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DisplayName("환불 동시성 통합 테스트")
class RefundServiceConcurrencyIntegrationTest extends IntegrationTestSupport {

    private static final int THREAD_COUNT = 20;
    private static final long INITIAL_BALANCE = 100L;
    private static final long OPTION_AMOUNT = 45L;

    @Autowired
    private RefundService refundService;

    @Autowired
    private MemberCommandJpaRepository memberCommandJpaRepository;

    @Autowired
    private MemberHeartBalanceRepository memberHeartBalanceRepository;

    @Autowired
    private OrderCommandRepository orderCommandRepository;

    @Autowired
    private RefundCommandRepository refundCommandRepository;

    @Autowired
    private HeartPurchaseOptionCommandRepository heartPurchaseOptionCommandRepository;

    private HeartPurchaseOption option;

    @AfterEach
    void tearDown() {
        memberHeartBalanceRepository.deleteAll();
        refundCommandRepository.deleteAll();
        orderCommandRepository.deleteAll();
        heartPurchaseOptionCommandRepository.delete(option);
        memberCommandJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("같은 환불 알림이 동시에 와도 모두 정상 종료하고 환불은 하나, 하트는 한 번만 회수된다")
    void refundOnceWithoutExceptionWhenNotifiedConcurrently() throws InterruptedException {
        // given
        Member member = memberCommandJpaRepository.save(MemberFixture.builder().build());
        saveHeartBalance(member.getId());
        option = heartPurchaseOptionCommandRepository.save(HeartPurchaseOptionFixture.builder()
            .amount(OPTION_AMOUNT)
            .build());
        Order order = orderCommandRepository.save(OrderFixture.builder()
            .memberId(member.getId())
            .build());
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
                        refundService.processRefund(transactionId, productId, 1, PaymentMethod.GOOGLE_PLAY,
                            NotificationType.REFUND);
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
        assertThat(refundCommandRepository.count()).isEqualTo(1);
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
            assertThat(purchaseHeartBalance(member)).isEqualTo(INITIAL_BALANCE - OPTION_AMOUNT));
        await().during(Duration.ofSeconds(1)).atMost(Duration.ofSeconds(2)).untilAsserted(() ->
            assertThat(purchaseHeartBalance(member)).isEqualTo(INITIAL_BALANCE - OPTION_AMOUNT));
    }

    private long purchaseHeartBalance(Member member) {
        return memberHeartBalanceRepository.findByMemberId(member.getId()).orElseThrow()
            .getPurchaseBalance();
    }

    private void saveHeartBalance(long memberId) {
        memberHeartBalanceRepository.save(MemberHeartBalanceFixture.builder()
            .memberId(memberId)
            .purchaseBalance(INITIAL_BALANCE)
            .build());
    }
}
