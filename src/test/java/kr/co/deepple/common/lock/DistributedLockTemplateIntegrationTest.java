package kr.co.deepple.common.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import kr.co.deepple.common.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@DisplayName("DistributedLockTemplate 통합 테스트")
class DistributedLockTemplateIntegrationTest extends IntegrationTestSupport {

  private static final int THREAD_COUNT = 10;
  private static final String KEY_PREFIX = "LOCK:";

  @Autowired private DistributedLockTemplate distributedLockTemplate;

  @Autowired private RedissonClient redissonClient;

  @Autowired private TransactionTemplate transactionTemplate;

  private volatile int counter;

  @Test
  @DisplayName("같은 키로 동시에 실행해도 한 번에 하나씩 실행된다")
  void runOneAtATime() throws InterruptedException {
    // given
    CountDownLatch doneSignal = new CountDownLatch(THREAD_COUNT);

    // when
    try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT)) {
      for (int i = 0; i < THREAD_COUNT; i++) {
        executor.submit(() -> {
          try {
            distributedLockTemplate.execute("counter", this::increaseSlowly);
          } finally {
            doneSignal.countDown();
          }
        });
      }
      assertThat(doneSignal.await(30, TimeUnit.SECONDS)).isTrue();
    }

    // then
    assertThat(counter).isEqualTo(THREAD_COUNT);
  }

  @Test
  @DisplayName("다른 스레드가 락을 쥐고 있으면 대기 시간이 지난 뒤 락 획득 실패 예외를 던진다")
  void throwWhenLockIsHeldByAnotherThread() throws InterruptedException {
    // given
    String key = "held";
    CountDownLatch lockAcquired = new CountDownLatch(1);
    CountDownLatch releaseSignal = new CountDownLatch(1);

    try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
      executor.submit(() ->
          distributedLockTemplate.execute(
              key,
              () -> {
                lockAcquired.countDown();
                awaitQuietly(releaseSignal);
              }));
      assertThat(lockAcquired.await(5, TimeUnit.SECONDS)).isTrue();

      // when & then
      assertThatThrownBy(() -> distributedLockTemplate.execute(key, () -> {}))
          .isInstanceOf(CannotAcquireLockException.class);

      releaseSignal.countDown();
    }
  }

  @Test
  @DisplayName("작업 중 예외는 감싸지 않고 그대로 던지고 락은 해제한다")
  void propagateActionExceptionAndReleaseLock() {
    // given
    String key = "failing";
    IllegalStateException actionException = new IllegalStateException("작업 실패");

    // when & then
    assertThatThrownBy(() ->
            distributedLockTemplate.execute(
                key,
                () -> {
                  throw actionException;
                }))
        .isSameAs(actionException);
    assertThat(getLock(key).isLocked()).isFalse();
  }

  @Test
  @DisplayName("트랜잭션 안에서 실행하고 락을 쥔 채로 커밋한 뒤 락을 해제한다")
  void commitWhileHoldingLock() {
    // given
    String key = "transaction";
    RLock lock = getLock(key);
    AtomicBoolean transactionActive = new AtomicBoolean();
    AtomicBoolean lockHeldAtCommit = new AtomicBoolean();

    // when
    distributedLockTemplate.executeInTransaction(
        key,
        () -> {
          transactionActive.set(TransactionSynchronizationManager.isActualTransactionActive());
          TransactionSynchronizationManager.registerSynchronization(
              new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                  lockHeldAtCommit.set(lock.isHeldByCurrentThread());
                }
              });
        });

    // then
    assertThat(transactionActive).isTrue();
    assertThat(lockHeldAtCommit).isTrue();
    assertThat(lock.isLocked()).isFalse();
  }

  @Test
  @DisplayName("이미 트랜잭션 안에서 호출하면 락을 잡지 않고 예외를 던진다")
  void throwWhenCalledInsideTransaction() {
    // given
    String key = "nested";

    // when & then
    assertThatThrownBy(() ->
            transactionTemplate.executeWithoutResult(
                status -> distributedLockTemplate.executeInTransaction(key, () -> {})))
        .isInstanceOf(IllegalStateException.class);
    assertThat(getLock(key).isLocked()).isFalse();
  }

  private RLock getLock(String key) {
    return redissonClient.getLock(KEY_PREFIX + key);
  }

  private void increaseSlowly() {
    int current = counter;
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    counter = current + 1;
  }

  private void awaitQuietly(CountDownLatch latch) {
    try {
      latch.await(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
