package kr.co.deepple.payment.adapter.out.kafka;

import kr.co.deepple.common.config.KafkaTopics;
import kr.co.deepple.payment.event.PaymentTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class PaymentKafkaTopicConfig {

  @Bean
  public KafkaAdmin.NewTopics paymentTopics() {
    return KafkaTopics.withDeadLetterTopic(PaymentTopic.NAME);
  }
}
