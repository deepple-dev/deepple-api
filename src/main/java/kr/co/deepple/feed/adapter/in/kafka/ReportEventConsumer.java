package kr.co.deepple.feed.adapter.in.kafka;

import kr.co.deepple.common.event.EventConsumer;
import kr.co.deepple.feed.application.port.in.HideReportedContentUseCase;
import kr.co.deepple.report.event.ReportAccumulatedEvent;
import kr.co.deepple.report.event.ReportTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("feedReportEventConsumer")
@RequiredArgsConstructor
@KafkaListener(topics = ReportTopic.NAME, groupId = FeedConsumerGroup.ID)
public class ReportEventConsumer extends EventConsumer {

  private final HideReportedContentUseCase hideReportedContentUseCase;

  @KafkaHandler
  public void handle(ReportAccumulatedEvent event) {
    switch (event.targetType()) {
      case "FEED" -> hideReportedContentUseCase.hideFeed(event.targetId());
      case "COMMENT" -> hideReportedContentUseCase.hideComment(event.targetId());
      default ->
          throw new IllegalArgumentException("숨길 수 없는 신고 대상입니다. targetType=" + event.targetType());
    }
  }
}
