package kr.co.deepple.auth.adapter.out.kafka;

import kr.co.deepple.auth.event.AuthTopic;
import kr.co.deepple.common.config.KafkaTopics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class AuthKafkaTopicConfig {

  @Bean
  public KafkaAdmin.NewTopics authTopics() {
    return KafkaTopics.withDeadLetterTopic(AuthTopic.NAME);
  }
}
