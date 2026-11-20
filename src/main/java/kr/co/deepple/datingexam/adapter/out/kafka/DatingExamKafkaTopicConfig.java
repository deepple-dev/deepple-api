package kr.co.deepple.datingexam.adapter.out.kafka;

import kr.co.deepple.common.config.KafkaTopics;
import kr.co.deepple.datingexam.event.DatingExamTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class DatingExamKafkaTopicConfig {

  @Bean
  public KafkaAdmin.NewTopics datingExamTopics() {
    return KafkaTopics.withDeadLetterTopic(DatingExamTopic.NAME);
  }
}
