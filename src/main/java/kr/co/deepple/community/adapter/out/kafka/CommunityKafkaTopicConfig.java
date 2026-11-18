package kr.co.deepple.community.adapter.out.kafka;

import kr.co.deepple.common.config.KafkaTopics;
import kr.co.deepple.community.event.CommunityTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class CommunityKafkaTopicConfig {

  @Bean
  public KafkaAdmin.NewTopics communityTopics() {
    return KafkaTopics.withDeadLetterTopic(CommunityTopic.NAME);
  }
}
