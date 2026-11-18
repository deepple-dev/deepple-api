package kr.co.deepple.heart.adapter.out.kafka;

import kr.co.deepple.common.config.KafkaTopics;
import kr.co.deepple.heart.event.HeartTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class HeartKafkaTopicConfig {

  @Bean
  public KafkaAdmin.NewTopics heartTopics() {
    return KafkaTopics.withDeadLetterTopic(HeartTopic.NAME);
  }
}
