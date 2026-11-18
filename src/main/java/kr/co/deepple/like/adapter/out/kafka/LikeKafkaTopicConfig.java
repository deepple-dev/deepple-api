package kr.co.deepple.like.adapter.out.kafka;

import kr.co.deepple.common.config.KafkaTopics;
import kr.co.deepple.like.event.LikeTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class LikeKafkaTopicConfig {

  @Bean
  public KafkaAdmin.NewTopics likeTopics() {
    return KafkaTopics.withDeadLetterTopic(LikeTopic.NAME);
  }
}
