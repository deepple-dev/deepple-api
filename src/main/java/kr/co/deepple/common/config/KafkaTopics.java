package kr.co.deepple.common.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KafkaTopics {

  private static final int PARTITIONS = 6;
  private static final String DEAD_LETTER_SUFFIX = "-dlt";

  public static KafkaAdmin.NewTopics withDeadLetterTopic(String name) {
    return new KafkaAdmin.NewTopics(
        TopicBuilder.name(name).partitions(PARTITIONS).build(),
        TopicBuilder.name(deadLetterTopic(name)).partitions(PARTITIONS).build());
  }

  public static String deadLetterTopic(String name) {
    return name + DEAD_LETTER_SUFFIX;
  }
}
