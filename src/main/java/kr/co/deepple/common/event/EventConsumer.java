package kr.co.deepple.common.event;

import org.springframework.kafka.annotation.KafkaHandler;

public abstract class EventConsumer {

  @KafkaHandler(isDefault = true)
  public void ignore(Object event) {}
}
