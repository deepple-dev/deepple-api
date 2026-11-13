package kr.co.deepple.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import kr.co.deepple.common.support.IntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@DisplayName("Event Publication Registry 통합 테스트")
class EventPublicationRegistryIntegrationTest extends IntegrationTestSupport {

  private static final String DEFAULT_LISTENER = "RegistryTestEventHandler.handle(";
  private static final Duration TIMEOUT = Duration.ofSeconds(10);
  private static final int MAX_ATTEMPTS = 10;

  @Autowired private ApplicationEventPublisher eventPublisher;

  @Autowired private TransactionTemplate transactionTemplate;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private RegistryTestEventHandler handler;

  @Autowired private FailedEventResubmitter failedEventResubmitter;

  @Autowired private CompletedEventPublicationCleaner completedEventPublicationCleaner;

  @AfterEach
  void tearDown() {
    jdbcTemplate.update(
        "delete from event_publication where event_type = ?", RegistryTestEvent.class.getName());
  }

  @Test
  @DisplayName("핸들러가 실패한 이벤트는 FAILED로 남고 재제출하면 다시 실행되어 완료 처리된다")
  void resubmitFailedPublication() {
    // given
    RegistryTestEvent event = publishFailedEvent();

    // when
    failedEventResubmitter.resubmit();

    // then
    awaitStatus(event.id(), DEFAULT_LISTENER, "COMPLETED");
    assertThat(handler.callCount(event.id())).isEqualTo(2);
    assertThat(findPublication(event.id(), DEFAULT_LISTENER).get("completion_attempts"))
        .isEqualTo(2);
  }

  @Test
  @DisplayName("시도 횟수를 다 쓴 이벤트는 재제출하지 않는다")
  void skipPublicationWhenAttemptsExhausted() {
    // given
    RegistryTestEvent event = publishFailedEvent();
    jdbcTemplate.update(
        "update event_publication set completion_attempts = ?"
            + " where serialized_event like ? and listener_id like ?",
        MAX_ATTEMPTS,
        like(event.id().toString()),
        like(DEFAULT_LISTENER));

    // when
    failedEventResubmitter.resubmit();

    // then
    await()
        .during(Duration.ofSeconds(2))
        .atMost(TIMEOUT)
        .untilAsserted(() ->
            assertThat(findPublication(event.id(), DEFAULT_LISTENER).get("status"))
                .isEqualTo("FAILED"));
    assertThat(handler.callCount(event.id())).isEqualTo(1);
  }

  @Test
  @DisplayName("칸이 추가되기 전에 저장된 이벤트 기록도 재제출하면 없는 칸을 null로 읽어 처리한다")
  void resubmitPublicationSerializedBeforeFieldWasAdded() {
    // given
    RegistryTestEvent event = publishFailedEvent();
    jdbcTemplate.update(
        "update event_publication set serialized_event = json_remove(serialized_event, '$.note')"
            + " where serialized_event like ? and listener_id like ?",
        like(event.id().toString()),
        like(DEFAULT_LISTENER));

    // when
    failedEventResubmitter.resubmit();

    // then
    awaitStatus(event.id(), DEFAULT_LISTENER, "COMPLETED");
    assertThat(handler.receivedNote(event.id())).isEqualTo("null");
  }

  @Test
  @DisplayName("완료된 지 7일이 지난 기록만 지운다")
  void cleanCompletedPublicationsOlderThanRetention() {
    // given
    RegistryTestEvent old = publishCompletedEvent();
    RegistryTestEvent recent = publishCompletedEvent();
    jdbcTemplate.update(
        "update event_publication set completion_date = date_sub(now(6), interval 8 day)"
            + " where serialized_event like ?",
        like(old.id().toString()));

    // when
    completedEventPublicationCleaner.clean();

    // then
    assertThat(countPublications(old.id())).isZero();
    assertThat(countPublications(recent.id())).isEqualTo(1);
  }

  private RegistryTestEvent publishCompletedEvent() {
    RegistryTestEvent event = RegistryTestEvent.of("note");
    publish(event);
    awaitStatus(event.id(), DEFAULT_LISTENER, "COMPLETED");
    return event;
  }

  private int countPublications(UUID eventId) {
    return jdbcTemplate.queryForObject(
        "select count(*) from event_publication where serialized_event like ?",
        Integer.class,
        like(eventId.toString()));
  }

  private RegistryTestEvent publishFailedEvent() {
    RegistryTestEvent event = RegistryTestEvent.of("note");
    handler.failFor(event.id());
    publish(event);
    awaitStatus(event.id(), DEFAULT_LISTENER, "FAILED");
    handler.succeedFor(event.id());
    return event;
  }

  private void publish(RegistryTestEvent event) {
    transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(event));
  }

  private void awaitStatus(UUID eventId, String listener, String status) {
    await()
        .atMost(TIMEOUT)
        .untilAsserted(
            () -> assertThat(findPublication(eventId, listener).get("status")).isEqualTo(status));
  }

  private Map<String, Object> findPublication(UUID eventId, String listener) {
    return jdbcTemplate.queryForMap(
        "select status, completion_attempts from event_publication"
            + " where serialized_event like ? and listener_id like ?",
        like(eventId.toString()),
        like(listener));
  }

  private String like(String value) {
    return "%" + value + "%";
  }
}
