package kr.co.deepple.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import kr.co.deepple.common.support.IntegrationTestSupport;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("스케줄러 잠금 통합 테스트")
class SchedulerLockConfigIntegrationTest extends IntegrationTestSupport {

  @Autowired private LockProvider lockProvider;

  @Test
  @DisplayName("같은 이름의 잠금은 한 번만 잡히고, 풀면 다시 잡힌다")
  void lockOnceUntilUnlocked() {
    // given
    String name = "test-" + UUID.randomUUID();

    // when
    Optional<SimpleLock> first = lockProvider.lock(configuration(name));
    Optional<SimpleLock> second = lockProvider.lock(configuration(name));
    first.orElseThrow().unlock();
    Optional<SimpleLock> third = lockProvider.lock(configuration(name));

    // then
    assertThat(second).isEmpty();
    assertThat(third).isPresent();
    third.orElseThrow().unlock();
  }

  private LockConfiguration configuration(String name) {
    return new LockConfiguration(Instant.now(), name, Duration.ofSeconds(30), Duration.ZERO);
  }
}
