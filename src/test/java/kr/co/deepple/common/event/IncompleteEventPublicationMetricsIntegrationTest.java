package kr.co.deepple.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("미완료 이벤트 발행 기록 지표 통합 테스트")
class IncompleteEventPublicationMetricsIntegrationTest
    extends EventPublicationIntegrationTestSupport {

  private static final String GAUGE = "event.publication.incomplete.oldest.age";

  @Autowired private IncompleteEventPublicationMetrics metrics;

  private SimpleMeterRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new SimpleMeterRegistry();
    metrics.bindTo(registry);
  }

  @Test
  @DisplayName("미완료 기록이 없으면 0을 낸다")
  void zeroWhenNoIncompletePublication() {
    // given
    publishCompletedEvent();

    // when
    double age = registry.get(GAUGE).gauge().value();

    // then
    assertThat(age).isZero();
  }

  @Test
  @DisplayName("방금 실패한 기록의 나이는 몇 초 안이다")
  void ageOfJustFailedPublication() {
    // given
    publishFailedEvent();

    // when
    double age = registry.get(GAUGE).gauge().value();

    // then
    assertThat(age).isBetween(0.0, 10.0);
  }

  @Test
  @DisplayName("완료된 기록은 빼고 가장 오래된 미완료 기록의 나이를 초로 낸다")
  void ageOfOldestIncompletePublication() {
    // given
    movePublicationDateBack(publishCompletedEvent().id(), Duration.ofHours(2));
    movePublicationDateBack(publishFailedEvent().id(), Duration.ofMinutes(40));
    movePublicationDateBack(publishFailedEvent().id(), Duration.ofMinutes(10));

    // when
    double age = registry.get(GAUGE).gauge().value();

    // then
    assertThat(age).isCloseTo(Duration.ofMinutes(40).toSeconds(), within(10.0));
  }

  private void movePublicationDateBack(UUID eventId, Duration duration) {
    jdbcTemplate.update(
        "update event_publication"
            + " set publication_date = date_sub(publication_date, interval ? second)"
            + " where serialized_event like ?",
        duration.toSeconds(),
        like(eventId));
  }
}
