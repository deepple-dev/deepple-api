package kr.co.deepple.block.adapter.in.kafka;

import kr.co.deepple.block.application.port.in.CreateBlockUseCase;
import kr.co.deepple.common.event.EventConsumer;
import kr.co.deepple.report.event.ReportCreatedEvent;
import kr.co.deepple.report.event.ReportTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("blockReportEventConsumer")
@RequiredArgsConstructor
@KafkaListener(topics = ReportTopic.NAME, groupId = BlockConsumerGroup.ID)
public class ReportEventConsumer extends EventConsumer {

  private final CreateBlockUseCase createBlockUseCase;

  @KafkaHandler
  public void handle(ReportCreatedEvent event) {
    createBlockUseCase.createBlock(event.reporterId(), event.reporteeId());
  }
}
