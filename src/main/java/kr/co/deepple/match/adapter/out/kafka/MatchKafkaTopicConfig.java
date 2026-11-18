package kr.co.deepple.match.adapter.out.kafka;

import kr.co.deepple.common.config.KafkaTopics;
import kr.co.deepple.match.event.MatchTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class MatchKafkaTopicConfig {

  @Bean
  public KafkaAdmin.NewTopics matchTopics() {
    return KafkaTopics.withDeadLetterTopic(MatchTopic.NAME);
  }
}
