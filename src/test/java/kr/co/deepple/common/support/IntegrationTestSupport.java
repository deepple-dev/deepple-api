package kr.co.deepple.common.support;

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import kr.co.deepple.common.config.KafkaContainerConfig;
import kr.co.deepple.common.config.MySqlContainerConfig;
import kr.co.deepple.common.config.S3ContainerConfig;
import kr.co.deepple.common.config.ValkeyContainerConfig;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@Import({
  MySqlContainerConfig.class,
  ValkeyContainerConfig.class,
  S3ContainerConfig.class,
  KafkaContainerConfig.class,
  KafkaConsumerLag.class
})
public abstract class IntegrationTestSupport {

  private static final Duration EVENT_PROCESSING_TIMEOUT = Duration.ofSeconds(30);
  private static final Set<String> SEEDED_TABLES =
      Set.of(
          "flyway_schema_history",
          "dating_exam_answer",
          "dating_exam_question",
          "dating_exam_subject",
          "feed_backgrounds",
          "heart_purchase_options",
          "heart_usage_policies",
          "interview_questions",
          "missions",
          "notification_templates");

  private static String[] cleanUpStatements;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private RedisConnectionFactory redisConnectionFactory;

  @Autowired private KafkaConsumerLag kafkaConsumerLag;

  @Autowired private ApplicationEventPublisher eventPublisher;

  @Autowired private TransactionTemplate transactionTemplate;

  @BeforeEach
  void cleanUpStorage() {
    boolean eventProcessingFinished = isEventProcessingFinished();
    jdbcTemplate.batchUpdate(findCleanUpStatements());
    try (RedisConnection connection = redisConnectionFactory.getConnection()) {
      connection.serverCommands().flushDb();
    }
    if (!eventProcessingFinished) {
      throw new IllegalStateException(
          "이전 테스트의 이벤트 처리가 " + EVENT_PROCESSING_TIMEOUT.toSeconds() + "초 안에 끝나지 않았습니다");
    }
  }

  protected void publishEvent(Object event) {
    transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(event));
  }

  protected void awaitEventProcessing() {
    await().atMost(EVENT_PROCESSING_TIMEOUT).ignoreExceptions().until(this::isEventProcessingIdle);
  }

  private boolean isEventProcessingIdle() throws ExecutionException, InterruptedException {
    long completedBefore = countCompletedEventPublications();
    return countInProgressEventPublications() == 0L
        && kafkaConsumerLag.isZero()
        && countInProgressEventPublications() == 0L
        && countCompletedEventPublications() == completedBefore;
  }

  private boolean isEventProcessingFinished() {
    try {
      awaitEventProcessing();
      return true;
    } catch (ConditionTimeoutException e) {
      return false;
    }
  }

  private long countInProgressEventPublications() {
    return jdbcTemplate.queryForObject(
        "select count(*) from event_publication"
            + " where status in ('PUBLISHED', 'PROCESSING', 'RESUBMITTED')",
        Long.class);
  }

  private long countCompletedEventPublications() {
    return jdbcTemplate.queryForObject(
        "select count(*) from event_publication where status = 'COMPLETED'", Long.class);
  }

  private String[] findCleanUpStatements() {
    if (cleanUpStatements == null) {
      cleanUpStatements =
          jdbcTemplate
              .queryForList(
                  "select table_name from information_schema.tables"
                      + " where table_schema = database() and table_type = 'BASE TABLE'",
                  String.class)
              .stream()
              .filter(table -> !SEEDED_TABLES.contains(table))
              .map(table -> "delete from `" + table + "`")
              .toArray(String[]::new);
    }
    return cleanUpStatements;
  }
}
