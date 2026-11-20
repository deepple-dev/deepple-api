package kr.co.deepple.block.adapter.out.kafka;

import kr.co.deepple.block.event.BlockTopic;
import kr.co.deepple.common.config.KafkaTopics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class BlockKafkaTopicConfig {

  @Bean
  public KafkaAdmin.NewTopics blockTopics() {
    return KafkaTopics.withDeadLetterTopic(BlockTopic.NAME);
  }
}
