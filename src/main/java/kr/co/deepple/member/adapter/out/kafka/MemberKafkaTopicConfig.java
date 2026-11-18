package kr.co.deepple.member.adapter.out.kafka;

import kr.co.deepple.common.config.KafkaTopics;
import kr.co.deepple.member.event.MemberTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class MemberKafkaTopicConfig {

  @Bean
  public KafkaAdmin.NewTopics memberTopics() {
    return KafkaTopics.withDeadLetterTopic(MemberTopic.NAME);
  }
}
