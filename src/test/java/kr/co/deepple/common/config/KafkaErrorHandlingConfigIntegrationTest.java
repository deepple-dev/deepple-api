package kr.co.deepple.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnectionAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.mapping.DefaultJacksonJavaTypeMapper;

@SpringBootTest(
    classes = {
      KafkaErrorHandlingConfig.class,
      KafkaErrorHandlingConfigIntegrationTest.TestListener.class
    })
@ImportAutoConfiguration({KafkaAutoConfiguration.class, ServiceConnectionAutoConfiguration.class})
@Import(KafkaContainerConfig.class)
class KafkaErrorHandlingConfigIntegrationTest {

  private static final String TOPIC = "kafka-error-handling-test";
  private static final String DLT = TOPIC + "-dlt";
  private static final Duration TIMEOUT = Duration.ofSeconds(60);

  @Autowired private KafkaTemplate<Object, Object> kafkaTemplate;

  @Autowired private ProducerFactory<Object, Object> producerFactory;

  @Autowired private ConsumerFactory<Object, Object> consumerFactory;

  @Autowired private TestListener listener;

  @Test
  @DisplayName("JSON에 모르는 칸이 있어도 이벤트 객체로 받는다.")
  void receiveEventIgnoringUnknownProperties() {
    String id = newId();
    ProducerRecord<Object, Object> record =
        new ProducerRecord<>(
            TOPIC, id, "{\"id\":\"" + id + "\",\"failure\":\"NONE\",\"unknown\":1}");
    record
        .headers()
        .add(
            DefaultJacksonJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME,
            TestEvent.class.getName().getBytes(StandardCharsets.UTF_8));

    stringTemplate().send(record);

    await().atMost(TIMEOUT).until(() -> listener.processed(id));
  }

  @ParameterizedTest
  @EnumSource(names = {"TRANSIENT", "DEEPPLE_UNAVAILABLE"})
  @DisplayName("일시적인 실패는 다섯 번 시도한 뒤 DLT로 보낸다.")
  void sendToDltAfterRetries(Failure failure) {
    String id = newId();

    kafkaTemplate.send(TOPIC, id, new TestEvent(id, failure));

    ConsumerRecord<Object, Object> dead = pollDlt(id);
    assertThat(listener.attempts(id)).isEqualTo(5);
    assertThat(new String((byte[]) dead.value(), StandardCharsets.UTF_8)).contains(id);
  }

  @ParameterizedTest
  @EnumSource(names = {"DEEPPLE", "ILLEGAL_ARGUMENT", "ILLEGAL_STATE"})
  @DisplayName("재시도해도 결과가 같은 예외는 한 번만 시도하고 DLT로 보낸다.")
  void sendToDltWithoutRetry(Failure failure) {
    String id = newId();

    kafkaTemplate.send(TOPIC, id, new TestEvent(id, failure));

    pollDlt(id);
    assertThat(listener.attempts(id)).isEqualTo(1);
  }

  @Test
  @DisplayName("역직렬화에 실패한 메시지는 리스너를 부르지 않고 원본 그대로 DLT로 보낸다.")
  void sendOriginalBytesToDltWhenDeserializationFails() {
    String id = newId();

    stringTemplate().send(TOPIC, id, "not-json");

    ConsumerRecord<Object, Object> dead = pollDlt(id);
    assertThat(dead.value()).isEqualTo("not-json".getBytes(StandardCharsets.UTF_8));
    assertThat(listener.attempts(id)).isZero();
  }

  private String newId() {
    return UUID.randomUUID().toString();
  }

  private KafkaTemplate<Object, Object> stringTemplate() {
    return new KafkaTemplate<>(
        producerFactory,
        Map.of(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class));
  }

  private ConsumerRecord<Object, Object> pollDlt(String key) {
    Properties overrides = new Properties();
    overrides.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
    try (Consumer<Object, Object> consumer =
        consumerFactory.createConsumer(newId(), null, null, overrides)) {
      consumer.subscribe(List.of(DLT));
      return await()
          .atMost(TIMEOUT)
          .until(
              () ->
                  StreamSupport.stream(consumer.poll(Duration.ofMillis(500)).spliterator(), false)
                      .filter(record -> key.equals(record.key()))
                      .findFirst()
                      .orElse(null),
              Objects::nonNull);
    }
  }

  static class TestListener {

    private final Map<String, AtomicInteger> attempts = new ConcurrentHashMap<>();
    private final Map<String, Boolean> processed = new ConcurrentHashMap<>();

    @KafkaListener(topics = TOPIC)
    void listen(TestEvent event) {
      attempts.computeIfAbsent(event.id(), id -> new AtomicInteger()).incrementAndGet();
      event.failure().throwIfAny();
      processed.put(event.id(), true);
    }

    int attempts(String id) {
      AtomicInteger count = attempts.get(id);
      return count == null ? 0 : count.get();
    }

    boolean processed(String id) {
      return processed.containsKey(id);
    }
  }

  record TestEvent(String id, Failure failure) {}

  enum Failure {
    NONE,
    TRANSIENT,
    DEEPPLE,
    DEEPPLE_UNAVAILABLE,
    ILLEGAL_ARGUMENT,
    ILLEGAL_STATE;

    void throwIfAny() {
      switch (this) {
        case NONE -> {}
        case TRANSIENT -> throw new RuntimeException("일시적 실패");
        case DEEPPLE -> throw new DeeppleException(TestErrorCode.FAILED);
        case DEEPPLE_UNAVAILABLE -> throw new DeeppleException(TestErrorCode.UNAVAILABLE);
        case ILLEGAL_ARGUMENT -> throw new IllegalArgumentException("잘못된 인자");
        case ILLEGAL_STATE -> throw new IllegalStateException("잘못된 상태");
      }
    }
  }

  @Getter
  @RequiredArgsConstructor
  enum TestErrorCode implements ErrorCode {
    FAILED(ErrorType.INVALID_ARGUMENT, "테스트 실패"),
    UNAVAILABLE(ErrorType.UNAVAILABLE, "테스트 외부 장애");

    private final ErrorType type;
    private final String message;
  }
}
