package kr.co.deepple.report.adapter.out.kafka;

import kr.co.deepple.common.config.KafkaTopics;
import kr.co.deepple.report.event.ReportTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class ReportKafkaTopicConfig {

  @Bean
  public KafkaAdmin.NewTopics reportTopics() {
    return KafkaTopics.withDeadLetterTopic(ReportTopic.NAME);
  }
}
