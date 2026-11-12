package kr.co.deepple.heart.application;

import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.heart.application.port.in.AddPurchaseHeartCommand;
import kr.co.deepple.heart.application.port.in.AddPurchaseHeartUseCase;
import kr.co.deepple.heart.application.port.in.UseHeartCommand;
import kr.co.deepple.heart.application.port.in.UseHeartUseCase;
import kr.co.deepple.heart.application.port.out.HeartMemberRepository;
import kr.co.deepple.heart.application.port.out.HeartTransactionRepository;
import kr.co.deepple.heart.application.port.out.MemberHeartBalanceRepository;
import kr.co.deepple.heart.fixture.HeartMemberFixture;
import kr.co.deepple.heart.fixture.MemberHeartBalanceFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("하트 잔액 동시성 통합 테스트")
class HeartTransactorConcurrencyIntegrationTest extends IntegrationTestSupport {
    private static final int THREAD_COUNT = 100;
    private static final long MEMBER_ID = 1L;
    private static final long INITIAL_BALANCE = 2000L;
    private static final long MESSAGE_PRICE = 20L;
    private static final long ADD_AMOUNT = 10L;

    @Autowired
    private UseHeartUseCase useHeartUseCase;

    @Autowired
    private AddPurchaseHeartUseCase addPurchaseHeartUseCase;

    @Autowired
    private MemberHeartBalanceRepository memberHeartBalanceRepository;

    @Autowired
    private HeartMemberRepository heartMemberRepository;

    @Autowired
    private HeartTransactionRepository heartTransactionRepository;

    @AfterEach
    void tearDown() {
        heartTransactionRepository.deleteAll();
        memberHeartBalanceRepository.deleteAll();
        heartMemberRepository.deleteAll();
    }

    @Test
    @DisplayName("같은 회원이 하트를 동시에 사용해도 사용한 만큼만 잔액이 줄어든다")
    void deductExactlyWhenUsedConcurrently() throws InterruptedException {
        // given
        saveHeartMember();
        saveBalance(INITIAL_BALANCE);

        // when
        AtomicInteger successCount = new AtomicInteger();
        runConcurrently(i -> {
            use(String.valueOf(i));
            successCount.incrementAndGet();
        });

        // then
        assertThat(totalBalance()).isEqualTo(INITIAL_BALANCE - successCount.get() * MESSAGE_PRICE);
        assertThat(heartTransactionRepository.count()).isEqualTo(successCount.get());
    }

    @Test
    @DisplayName("같은 멱등 키로 하트 사용이 동시에 요청돼도 한 번만 차감된다")
    void deductOnceWhenSameKeyUsedConcurrently() throws InterruptedException {
        // given
        saveHeartMember();
        saveBalance(INITIAL_BALANCE);

        // when
        runConcurrently(i -> use("same-key"));

        // then
        assertThat(totalBalance()).isEqualTo(INITIAL_BALANCE - MESSAGE_PRICE);
        assertThat(heartTransactionRepository.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("같은 회원에게 하트 사용과 지급이 동시에 일어나도 잔액과 거래 내역이 맞는다")
    void keepExactBalanceWhenUsedAndAddedConcurrently() throws InterruptedException {
        // given
        saveHeartMember();
        saveBalance(INITIAL_BALANCE);

        // when
        AtomicInteger useCount = new AtomicInteger();
        AtomicInteger addCount = new AtomicInteger();
        runConcurrently(i -> {
            if (i % 2 == 0) {
                use(String.valueOf(i));
                useCount.incrementAndGet();
            } else {
                addPurchaseHeartUseCase.addPurchaseHeart(new AddPurchaseHeartCommand(MEMBER_ID, i, ADD_AMOUNT));
                addCount.incrementAndGet();
            }
        });

        // then
        long expected = INITIAL_BALANCE - useCount.get() * MESSAGE_PRICE + addCount.get() * ADD_AMOUNT;
        assertThat(totalBalance()).isEqualTo(expected);
        assertThat(heartTransactionRepository.count()).isEqualTo(useCount.get() + addCount.get());
    }

    private void runConcurrently(IntConsumer task) throws InterruptedException {
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(THREAD_COUNT);
        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT)) {
            for (int i = 0; i < THREAD_COUNT; i++) {
                int index = i;
                executor.submit(() -> {
                    try {
                        startSignal.await();
                        task.accept(index);
                    } catch (Exception ignored) {
                        // 락 대기 초과 등으로 실패한 요청은 성공 횟수에 넣지 않는다
                    } finally {
                        doneSignal.countDown();
                    }
                });
            }
            startSignal.countDown();
            assertThat(doneSignal.await(30, TimeUnit.SECONDS)).isTrue();
        }
    }

    private void use(String idempotencyKey) {
        useHeartUseCase.useHeart(new UseHeartCommand(MEMBER_ID, "MESSAGE", "MATCH", null, idempotencyKey));
    }

    private void saveHeartMember() {
        heartMemberRepository.save(HeartMemberFixture.builder().memberId(MEMBER_ID).build());
    }

    private void saveBalance(long purchaseHeartBalance) {
        memberHeartBalanceRepository.save(MemberHeartBalanceFixture.builder()
            .memberId(MEMBER_ID)
            .purchaseBalance(purchaseHeartBalance)
            .build());
    }

    private long totalBalance() {
        return memberHeartBalanceRepository.findByMemberId(MEMBER_ID).orElseThrow()
            .getTotalBalance();
    }
}
