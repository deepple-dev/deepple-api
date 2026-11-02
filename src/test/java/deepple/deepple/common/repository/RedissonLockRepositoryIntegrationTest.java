package deepple.deepple.common.repository;

import deepple.deepple.common.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RedissonLockRepository 통합 테스트")
class RedissonLockRepositoryIntegrationTest extends IntegrationTestSupport {

    private static final int THREAD_COUNT = 10;

    @Autowired
    private RedissonLockRepository redissonLockRepository;

    private volatile int counter;

    @Test
    @DisplayName("같은 키로 동시에 실행해도 한 번에 하나씩 실행된다")
    void runOneAtATime() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT)) {
            for (int i = 0; i < THREAD_COUNT; i++) {
                executor.submit(() -> {
                    try {
                        redissonLockRepository.withLock(this::increaseSlowly, "counter", 10, 5);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await(30, TimeUnit.SECONDS);
        }

        assertThat(counter).isEqualTo(THREAD_COUNT);
    }

    // 읽고 쓰는 사이에 다른 스레드가 끼어들 틈을 만든다
    private void increaseSlowly() {
        int current = counter;
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        counter = current + 1;
    }
}
