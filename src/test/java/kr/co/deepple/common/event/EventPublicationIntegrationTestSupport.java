package kr.co.deepple.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import kr.co.deepple.common.support.IntegrationTestSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

abstract class EventPublicationIntegrationTestSupport extends IntegrationTestSupport {

  private static final Duration TIMEOUT = Duration.ofSeconds(10);

  @Autowired protected JdbcTemplate jdbcTemplate;

  @Autowired protected RegistryTestEventHandler handler;

  protected RegistryTestEvent publishCompletedEvent() {
    RegistryTestEvent event = RegistryTestEvent.of("note");
    publishEvent(event);
    awaitStatus(event.id(), "COMPLETED");
    return event;
  }

  protected RegistryTestEvent publishFailedEvent() {
    RegistryTestEvent event = RegistryTestEvent.of("note");
    handler.failFor(event.id());
    publishEvent(event);
    awaitStatus(event.id(), "FAILED");
    return event;
  }

  protected void awaitStatus(UUID eventId, String status) {
    await()
        .atMost(TIMEOUT)
        .untilAsserted(() -> assertThat(findPublication(eventId).get("status")).isEqualTo(status));
  }

  protected Map<String, Object> findPublication(UUID eventId) {
    return jdbcTemplate.queryForMap(
        "select status, completion_attempts from event_publication where serialized_event like ?",
        like(eventId));
  }

  protected String like(UUID eventId) {
    return "%" + eventId + "%";
  }
}
