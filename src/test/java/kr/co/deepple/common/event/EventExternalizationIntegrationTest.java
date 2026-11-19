package kr.co.deepple.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.StreamSupport;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.member.event.MemberLoggedInEvent;
import kr.co.deepple.member.event.MemberTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import tools.jackson.databind.json.JsonMapper;

@DisplayName("이벤트 Kafka 외부화 통합 테스트")
class EventExternalizationIntegrationTest extends IntegrationTestSupport {

  private static final Duration TIMEOUT = Duration.ofSeconds(30);

  @Autowired private ConsumerFactory<Object, Object> consumerFactory;

  @Autowired private JsonMapper jsonMapper;

  @Test
  @DisplayName("이벤트를 발행하면 발행 모듈 토픽에 키와 앱 JsonMapper로 쓴 JSON을 보낸다")
  void externalizeEventToKafka() {
    // given
    MemberLoggedInEvent event = MemberLoggedInEvent.from(7L);

    // when
    publishEvent(event);

    // then
    ConsumerRecord<Object, Object> record = pollRecord(MemberTopic.NAME, event.id());
    assertThat(record.key()).isEqualTo("7");
    assertThat(jsonMapper.readValue((byte[]) record.value(), MemberLoggedInEvent.class))
        .isEqualTo(event);
  }

  private ConsumerRecord<Object, Object> pollRecord(String topic, UUID eventId) {
    Properties overrides = new Properties();
    overrides.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
    try (Consumer<Object, Object> consumer =
        consumerFactory.createConsumer(UUID.randomUUID().toString(), null, null, overrides)) {
      consumer.subscribe(List.of(topic));
      return await()
          .atMost(TIMEOUT)
          .until(
              () ->
                  StreamSupport.stream(consumer.poll(Duration.ofMillis(500)).spliterator(), false)
                      .filter(record -> hasEventId(record, eventId))
                      .findFirst()
                      .orElse(null),
              Objects::nonNull);
    }
  }

  private boolean hasEventId(ConsumerRecord<Object, Object> record, UUID eventId) {
    return eventId
        .toString()
        .equals(
            jsonMapper.readTree((byte[]) record.value()).path("metadata").path("id").asString());
  }
}
