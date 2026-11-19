package kr.co.deepple.common.event;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IncompleteEventPublicationMetrics implements MeterBinder {

  private static final String OLDEST_PUBLICATION_DATE_SQL =
      "select min(publication_date) from event_publication where completion_date is null";

  private final EntityManager entityManager;

  @Override
  public void bindTo(MeterRegistry registry) {
    Gauge.builder(
            "event.publication.incomplete.oldest.age",
            this,
            IncompleteEventPublicationMetrics::oldestAgeSeconds)
        .description("가장 오래된 미완료 이벤트 발행 기록의 나이")
        .baseUnit("seconds")
        .register(registry);
  }

  double oldestAgeSeconds() {
    Instant oldest =
        (Instant)
            entityManager
                .createNativeQuery(OLDEST_PUBLICATION_DATE_SQL, Instant.class)
                .getSingleResult();
    if (oldest == null) {
      return 0;
    }
    return Duration.between(oldest, Instant.now()).toSeconds();
  }
}
