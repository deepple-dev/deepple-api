package deepple.deepple.member.command.infra.member;

import deepple.deepple.common.support.IntegrationTestSupport;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import deepple.deepple.member.fixture.MemberFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("회원 행 락 대기 통합 테스트")
class MemberRowLockWaitIntegrationTest extends IntegrationTestSupport {

    private static final Duration MAX_EXPECTED_WAIT = Duration.ofSeconds(5);

    @Autowired
    private MemberCommandRepository memberCommandRepository;

    @Autowired
    private MemberCommandJpaRepository memberCommandJpaRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @AfterEach
    void tearDown() {
        memberCommandJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("다른 트랜잭션이 회원 행을 잠그고 있으면 오래 기다리지 않고 동시성 실패 예외를 던진다")
    void giveUpWaitingForLockedMemberRow() throws InterruptedException {
        // given
        long memberId = memberCommandJpaRepository.save(MemberFixture.builder().build()).getId();
        CountDownLatch rowLocked = new CountDownLatch(1);
        CountDownLatch releaseSignal = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> transactionTemplate.executeWithoutResult(status -> {
                memberCommandRepository.findByIdForUpdate(memberId);
                rowLocked.countDown();
                awaitQuietly(releaseSignal);
            }));
            assertThat(rowLocked.await(5, TimeUnit.SECONDS)).isTrue();

            // when
            long startNanos = System.nanoTime();
            Throwable thrown = catchLockWait(memberId);
            Duration elapsed = Duration.ofNanos(System.nanoTime() - startNanos);
            releaseSignal.countDown();

            // then
            assertThat(thrown).isInstanceOf(ConcurrencyFailureException.class);
            assertThat(elapsed).isLessThan(MAX_EXPECTED_WAIT);
        }
    }

    private Throwable catchLockWait(long memberId) {
        try {
            transactionTemplate.executeWithoutResult(status -> memberCommandRepository.findByIdForUpdate(memberId));
            return null;
        } catch (RuntimeException e) {
            return e;
        }
    }

    private void awaitQuietly(CountDownLatch latch) {
        try {
            latch.await(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
