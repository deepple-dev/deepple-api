package kr.co.deepple.block.adapter.in.kafka;

import kr.co.deepple.block.application.port.in.BlockCommander;
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

  private final BlockCommander blockCommander;

  @KafkaHandler
  public void handle(ReportCreatedEvent event) {
    blockCommander.createBlockIfAbsent(event.reporterId(), event.reporteeId());
  }
}
