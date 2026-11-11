package deepple.deepple.common.config;

import deepple.deepple.common.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Redis 응답 지연 타임아웃 통합 테스트")
class RedisTimeoutIntegrationTest extends IntegrationTestSupport {

    private static final Duration MAX_WAIT = Duration.ofSeconds(13);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private GenericContainer<?> valkeyContainer;

    @Test
    @DisplayName("Redis가 응답하지 않으면 기본 재시도를 포함해 13초 안에 실패한다")
    void failQuicklyWhenRedisDoesNotRespond() {
        // given
        stringRedisTemplate.opsForValue().get("warm-up");
        String containerId = valkeyContainer.getContainerId();
        DockerClientFactory.instance().client().pauseContainerCmd(containerId).exec();

        try {
            // when
            long startedAt = System.nanoTime();
            assertThatThrownBy(() -> stringRedisTemplate.opsForValue().get("timeout-test"))
                .isInstanceOf(RuntimeException.class);
            Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);

            // then
            assertThat(elapsed).isLessThan(MAX_WAIT);
        } finally {
            DockerClientFactory.instance().client().unpauseContainerCmd(containerId).exec();
        }
    }
}
