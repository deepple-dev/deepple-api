package kr.co.deepple.admin.adapter.out.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import kr.co.deepple.admin.domain.AdminErrorCode;
import kr.co.deepple.common.config.KafkaTopics;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.IntegrationTestSupport;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;

class KafkaDeadLetterReplayerIntegrationTest extends IntegrationTestSupport {

  private static final String TYPE_ID_HEADER = "__TypeId__";
  private static final Duration TIMEOUT = Duration.ofSeconds(30);

  @Autowired private KafkaDeadLetterReplayer replayer;

  @Autowired private KafkaTemplate<Object, Object> kafkaTemplate;

  @Autowired private ConsumerFactory<Object, Object> consumerFactory;

  @Test
  @DisplayName("DLT 메시지를 키·값·헤더 그대로 원래 토픽에 다시 넣고 DLT 헤더는 뺀다.")
  void replayToOriginalTopic() throws Exception {
    String topic = newTopic();
    sendToDeadLetterTopic(topic, "1", "first");
    sendToDeadLetterTopic(topic, "2", "second");

    long replayed = replayer.replay(topic);

    assertThat(replayed).isEqualTo(2L);
    List<ConsumerRecord<Object, Object>> records = poll(topic, 2);
    assertThat(records)
        .extracting(ConsumerRecord::key, record -> text(record.value()))
        .containsExactlyInAnyOrder(tuple("1", "first"), tuple("2", "second"));
    assertThat(records)
        .allSatisfy(record ->
            assertThat(text(record.headers().lastHeader(TYPE_ID_HEADER).value()))
                .isEqualTo("kr.co.deepple.test.TestEvent"));
  }

  @Test
  @DisplayName("이미 다시 넣은 DLT 메시지는 다음 호출에서 다시 넣지 않는다.")
  void replayOnlyNewMessages() throws Exception {
    String topic = newTopic();
    sendToDeadLetterTopic(topic, "1", "first");
    replayer.replay(topic);

    assertThat(replayer.replay(topic)).isZero();

    sendToDeadLetterTopic(topic, "2", "second");
    assertThat(replayer.replay(topic)).isEqualTo(1L);
    assertThat(poll(topic, 2))
        .extracting(record -> text(record.value()))
        .containsExactlyInAnyOrder("first", "second");
  }

  @Test
  @DisplayName("DLT가 없는 토픽이면 DEAD_LETTER_TOPIC_NOT_FOUND 예외를 던진다.")
  void throwWhenDeadLetterTopicNotFound() {
    assertThatThrownBy(() -> replayer.replay(newTopic()))
        .isInstanceOfSatisfying(
            DeeppleException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(AdminErrorCode.DEAD_LETTER_TOPIC_NOT_FOUND));
  }

  private String newTopic() {
    return "dead-letter-replay-test-" + UUID.randomUUID();
  }

  private void sendToDeadLetterTopic(String topic, String key, String value) throws Exception {
    RecordHeaders headers = new RecordHeaders();
    headers.add(TYPE_ID_HEADER, "kr.co.deepple.test.TestEvent".getBytes(StandardCharsets.UTF_8));
    headers.add(KafkaHeaders.DLT_ORIGINAL_TOPIC, topic.getBytes(StandardCharsets.UTF_8));
    kafkaTemplate
        .send(
            new ProducerRecord<>(
                KafkaTopics.deadLetterTopic(topic),
                null,
                key,
                value.getBytes(StandardCharsets.UTF_8),
                headers))
        .get();
  }

  private List<ConsumerRecord<Object, Object>> poll(String topic, int expected) {
    Properties overrides = new Properties();
    overrides.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
    overrides.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    List<ConsumerRecord<Object, Object>> records = new ArrayList<>();
    try (Consumer<Object, Object> consumer =
        consumerFactory.createConsumer(UUID.randomUUID().toString(), null, null, overrides)) {
      consumer.subscribe(List.of(topic));
      await()
          .atMost(TIMEOUT)
          .until(() -> {
            consumer.poll(Duration.ofMillis(500)).forEach(records::add);
            return records.size() >= expected;
          });
      consumer.poll(Duration.ofMillis(500)).forEach(records::add);
    }
    return records;
  }

  private String text(Object bytes) {
    return new String((byte[]) bytes, StandardCharsets.UTF_8);
  }
}
