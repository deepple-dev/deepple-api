package kr.co.deepple.admin.adapter.out.kafka;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import kr.co.deepple.admin.application.port.out.DeadLetterReplayer;
import kr.co.deepple.admin.domain.AdminErrorCode;
import kr.co.deepple.common.config.KafkaTopics;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaDeadLetterReplayer implements DeadLetterReplayer {

  private static final String GROUP_ID = "dead-letter-replay";
  private static final Duration POLL_TIMEOUT = Duration.ofMillis(500);
  private static final String DEAD_LETTER_HEADER_PREFIX = "kafka_dlt-";

  private final ConsumerFactory<Object, Object> consumerFactory;
  private final KafkaTemplate<Object, Object> kafkaTemplate;

  @Override
  public long replay(String topic) {
    String deadLetterTopic = KafkaTopics.deadLetterTopic(topic);
    try (Consumer<Object, Object> consumer =
        consumerFactory.createConsumer(GROUP_ID, null, null, consumerProperties())) {
      List<TopicPartition> partitions = partitionsOf(consumer, deadLetterTopic);
      consumer.assign(partitions);
      Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);

      List<CompletableFuture<?>> sends = new ArrayList<>();
      while (!reachedEnd(consumer, endOffsets)) {
        for (ConsumerRecord<Object, Object> record : consumer.poll(POLL_TIMEOUT)) {
          if (record.offset() < endOffsets.get(partitionOf(record))) {
            sends.add(kafkaTemplate.send(toOriginalTopic(topic, record)));
          }
        }
      }
      CompletableFuture.allOf(sends.toArray(CompletableFuture[]::new)).join();
      consumer.commitSync(toCommitOffsets(endOffsets));
      return sends.size();
    }
  }

  private Properties consumerProperties() {
    Properties properties = new Properties();
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    properties.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, false);
    return properties;
  }

  private List<TopicPartition> partitionsOf(Consumer<?, ?> consumer, String deadLetterTopic) {
    List<PartitionInfo> partitions = consumer.partitionsFor(deadLetterTopic);
    if (partitions == null || partitions.isEmpty()) {
      throw new DeeppleException(
          AdminErrorCode.DEAD_LETTER_TOPIC_NOT_FOUND, "topic=" + deadLetterTopic);
    }
    return partitions.stream()
        .map(partition -> new TopicPartition(partition.topic(), partition.partition()))
        .toList();
  }

  private boolean reachedEnd(Consumer<?, ?> consumer, Map<TopicPartition, Long> endOffsets) {
    return endOffsets.entrySet().stream()
        .allMatch(entry -> consumer.position(entry.getKey()) >= entry.getValue());
  }

  private TopicPartition partitionOf(ConsumerRecord<?, ?> record) {
    return new TopicPartition(record.topic(), record.partition());
  }

  private ProducerRecord<Object, Object> toOriginalTopic(
      String topic, ConsumerRecord<Object, Object> record) {
    RecordHeaders headers = new RecordHeaders();
    for (Header header : record.headers()) {
      if (!header.key().startsWith(DEAD_LETTER_HEADER_PREFIX)) {
        headers.add(header);
      }
    }
    return new ProducerRecord<>(topic, null, record.key(), record.value(), headers);
  }

  private Map<TopicPartition, OffsetAndMetadata> toCommitOffsets(
      Map<TopicPartition, Long> endOffsets) {
    return endOffsets.entrySet().stream()
        .collect(
            Collectors.toMap(Map.Entry::getKey, entry -> new OffsetAndMetadata(entry.getValue())));
  }
}
