package deepple.deepple.common.lock;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockTemplate {

    private static final String KEY_PREFIX = "LOCK:";
    private static final long WAIT_SECONDS = 3L;

    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate;

    public void execute(@NonNull String key, @NonNull Runnable action) {
        RLock lock = redissonClient.getLock(KEY_PREFIX + key);
        if (!tryLock(lock, key)) {
            throw new CannotAcquireLockException("락을 획득하지 못했습니다. key=" + key);
        }
        try {
            action.run();
        } finally {
            unlock(lock, key);
        }
    }

    public void executeInTransaction(@NonNull String key, @NonNull Runnable action) {
        validateNoActiveTransaction(key);
        execute(key, () -> transactionTemplate.executeWithoutResult(status -> action.run()));
    }

    private boolean tryLock(RLock lock, String key) {
        try {
            return lock.tryLock(WAIT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CannotAcquireLockException("락을 기다리는 중 인터럽트되었습니다. key=" + key, e);
        }
    }

    private void unlock(RLock lock, String key) {
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                return;
            }
            log.atWarn()
                .addKeyValue("lockKey", key)
                .log("작업이 끝나기 전에 분산 락이 해제됨");
        } catch (RuntimeException e) {
            log.atWarn()
                .addKeyValue("lockKey", key)
                .setCause(e)
                .log("분산 락 해제 실패");
        }
    }

    private void validateNoActiveTransaction(String key) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("트랜잭션 안에서는 분산 락을 잡을 수 없습니다. key=" + key);
        }
    }
}
