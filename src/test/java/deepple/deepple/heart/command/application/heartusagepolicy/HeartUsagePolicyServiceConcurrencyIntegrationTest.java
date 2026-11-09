package deepple.deepple.heart.command.application.heartusagepolicy;

import deepple.deepple.common.support.IntegrationTestSupport;
import deepple.deepple.heart.command.domain.hearttransaction.HeartTransactionCommandRepository;
import deepple.deepple.heart.command.domain.hearttransaction.vo.TransactionSubtype;
import deepple.deepple.heart.command.domain.hearttransaction.vo.TransactionType;
import deepple.deepple.member.command.application.member.MemberHeartBalanceService;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.infra.member.MemberCommandJpaRepository;
import deepple.deepple.member.fixture.MemberFixture;
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

@DisplayName("하트 사용 동시성 통합 테스트")
class HeartUsagePolicyServiceConcurrencyIntegrationTest extends IntegrationTestSupport {

    private static final int THREAD_COUNT = 100;
    private static final long INITIAL_BALANCE = 2000L;
    private static final long MESSAGE_PRICE = 20L;
    private static final long GRANT_AMOUNT = 10L;

    @Autowired
    private HeartUsagePolicyService heartUsagePolicyService;

    @Autowired
    private MemberHeartBalanceService memberHeartBalanceService;

    @Autowired
    private MemberCommandJpaRepository memberCommandJpaRepository;

    @Autowired
    private HeartTransactionCommandRepository heartTransactionCommandRepository;

    @AfterEach
    void tearDown() {
        heartTransactionCommandRepository.deleteAll();
        memberCommandJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("같은 회원이 하트를 동시에 사용해도 사용한 만큼만 잔액이 줄어든다")
    void deductExactlyWhenUsedConcurrently() throws InterruptedException {
        // given
        Member member = memberCommandJpaRepository.save(MemberFixture.builder()
            .purchaseHeartBalance(INITIAL_BALANCE)
            .build());
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
                        heartUsagePolicyService.useHeart(memberId, TransactionType.MESSAGE, "동시성 테스트",
                            TransactionSubtype.MATCH.name());
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
        Member result = memberCommandJpaRepository.findById(memberId).orElseThrow();
        long remaining = result.getHeartBalance().getTotalHeartBalance();

        assertThat(successCount.get() + failureCount.get()).isEqualTo(THREAD_COUNT);
        assertThat(remaining).isEqualTo(INITIAL_BALANCE - successCount.get() * MESSAGE_PRICE);
        assertThat(heartTransactionCommandRepository.count()).isEqualTo(successCount.get());
    }

    @Test
    @DisplayName("같은 회원에게 하트 사용과 지급이 동시에 일어나도 잔액이 정확하다")
    void keepExactBalanceWhenUsedAndGrantedConcurrently() throws InterruptedException {
        // given
        Member member = memberCommandJpaRepository.save(MemberFixture.builder()
            .purchaseHeartBalance(INITIAL_BALANCE)
            .build());
        long memberId = member.getId();

        // when
        AtomicInteger useSuccessCount = new AtomicInteger();
        AtomicInteger grantSuccessCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(THREAD_COUNT);

        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT)) {
            for (int i = 0; i < THREAD_COUNT; i++) {
                boolean isUse = i % 2 == 0;
                executor.submit(() -> {
                    try {
                        startSignal.await();
                        if (isUse) {
                            heartUsagePolicyService.useHeart(memberId, TransactionType.MESSAGE, "동시성 테스트",
                                TransactionSubtype.MATCH.name());
                            useSuccessCount.incrementAndGet();
                        } else {
                            memberHeartBalanceService.grantPurchasedHearts(memberId, GRANT_AMOUNT);
                            grantSuccessCount.incrementAndGet();
                        }
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
        Member result = memberCommandJpaRepository.findById(memberId).orElseThrow();
        long remaining = result.getHeartBalance().getTotalHeartBalance();
        long expected = INITIAL_BALANCE - useSuccessCount.get() * MESSAGE_PRICE + grantSuccessCount.get() * GRANT_AMOUNT;
        int successCount = useSuccessCount.get() + grantSuccessCount.get();

        assertThat(successCount + failureCount.get()).isEqualTo(THREAD_COUNT);
        assertThat(remaining).isEqualTo(expected);
        await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> assertThat(heartTransactionCommandRepository.count()).isEqualTo(successCount));
    }
}
