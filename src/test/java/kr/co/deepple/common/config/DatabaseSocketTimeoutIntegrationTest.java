package kr.co.deepple.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import kr.co.deepple.common.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

@DisplayName("DB 응답 지연 타임아웃 통합 테스트")
class DatabaseSocketTimeoutIntegrationTest extends IntegrationTestSupport {

  private static final Duration MAX_WAIT = Duration.ofSeconds(12);

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  @DisplayName("DB가 응답하지 않으면 소켓 타임아웃 안에 실패한다")
  void failWithinSocketTimeoutWhenDatabaseDoesNotRespond() {
    // when
    long startedAt = System.nanoTime();
    assertThatThrownBy(() -> jdbcTemplate.queryForObject("SELECT SLEEP(15)", Integer.class))
        .isInstanceOf(DataAccessException.class);
    Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);

    // then
    assertThat(elapsed).isLessThan(MAX_WAIT);
  }
}
