package kr.co.deepple.admin.adapter.out.kafka;

import kr.co.deepple.admin.event.AdminTopic;
import kr.co.deepple.common.config.KafkaTopics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class AdminKafkaTopicConfig {

  @Bean
  public KafkaAdmin.NewTopics adminTopics() {
    return KafkaTopics.withDeadLetterTopic(AdminTopic.NAME);
  }
}
