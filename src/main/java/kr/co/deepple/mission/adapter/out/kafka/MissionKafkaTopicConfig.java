package kr.co.deepple.mission.adapter.out.kafka;

import kr.co.deepple.common.config.KafkaTopics;
import kr.co.deepple.mission.event.MissionTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class MissionKafkaTopicConfig {

  @Bean
  public KafkaAdmin.NewTopics missionTopics() {
    return KafkaTopics.withDeadLetterTopic(MissionTopic.NAME);
  }
}
