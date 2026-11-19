package kr.co.deepple.common.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.RetryListener;
import org.springframework.kafka.support.KafkaUtils;

@Slf4j
class DeadLetterRecoveryListener implements RetryListener {

  private final Counter deadLetters;

  DeadLetterRecoveryListener(MeterRegistry meterRegistry) {
    this.deadLetters =
        Counter.builder("kafka.dlt.records")
            .description("DLT로 보낸 Kafka 메시지 수")
            .register(meterRegistry);
  }

  @Override
  public void failedDelivery(ConsumerRecord<?, ?> record, Exception ex, int deliveryAttempt) {}

  @Override
  public void recovered(ConsumerRecord<?, ?> record, Exception ex) {
    deadLetters.increment();
    log.atError()
        .setCause(ex)
        .addKeyValue("topic", record.topic())
        .addKeyValue("partition", record.partition())
        .addKeyValue("offset", record.offset())
        .addKeyValue("consumerGroup", KafkaUtils.getConsumerGroupId())
        .log("Kafka 메시지를 DLT로 보냄");
  }
}
