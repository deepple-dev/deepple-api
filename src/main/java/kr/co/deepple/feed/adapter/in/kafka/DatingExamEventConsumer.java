package kr.co.deepple.feed.adapter.in.kafka;

import kr.co.deepple.common.event.EventConsumer;
import kr.co.deepple.datingexam.event.DatingExamTopic;
import kr.co.deepple.datingexam.event.DominantPersonalityTypeChangedEvent;
import kr.co.deepple.feed.application.port.in.SyncFeedAuthorUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("feedDatingExamEventConsumer")
@RequiredArgsConstructor
@KafkaListener(topics = DatingExamTopic.NAME, groupId = FeedConsumerGroup.ID)
public class DatingExamEventConsumer extends EventConsumer {

  private final SyncFeedAuthorUseCase syncFeedAuthorUseCase;

  @KafkaHandler
  public void handle(DominantPersonalityTypeChangedEvent event) {
    syncFeedAuthorUseCase.syncPersonalityType(
        event.memberId(), event.personalityType(), event.occurredAt());
  }
}
