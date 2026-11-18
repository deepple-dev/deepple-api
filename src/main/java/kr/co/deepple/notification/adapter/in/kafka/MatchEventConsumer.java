package kr.co.deepple.notification.adapter.in.kafka;

import java.util.Map;
import kr.co.deepple.common.event.EventConsumer;
import kr.co.deepple.match.event.MatchAcceptedEvent;
import kr.co.deepple.match.event.MatchRejectedEvent;
import kr.co.deepple.match.event.MatchRequestedEvent;
import kr.co.deepple.match.event.MatchTopic;
import kr.co.deepple.notification.application.NotificationSendService;
import kr.co.deepple.notification.application.dto.NotificationSendRequest;
import kr.co.deepple.notification.domain.ChannelType;
import kr.co.deepple.notification.domain.NotificationType;
import kr.co.deepple.notification.domain.SenderType;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("notificationMatchEventConsumer")
@RequiredArgsConstructor
@KafkaListener(topics = MatchTopic.NAME, groupId = NotificationConsumerGroup.ID)
public class MatchEventConsumer extends EventConsumer {

  private final NotificationSendService notificationSendService;

  @KafkaHandler
  public void handle(MatchRequestedEvent event) {
    notificationSendService.send(
        new NotificationSendRequest(
            event.id().toString(),
            SenderType.MEMBER,
            event.requesterId(),
            event.responderId(),
            NotificationType.MATCH_REQUEST,
            Map.of(NotificationSendRequest.SENDER_NAME, event.requesterName()),
            ChannelType.PUSH));
  }

  @KafkaHandler
  public void handle(MatchAcceptedEvent event) {
    notificationSendService.send(
        new NotificationSendRequest(
            event.id().toString(),
            SenderType.MEMBER,
            event.responderId(),
            event.requesterId(),
            NotificationType.MATCH_ACCEPT,
            Map.of(NotificationSendRequest.SENDER_NAME, event.responderName()),
            ChannelType.PUSH));
  }

  @KafkaHandler
  public void handle(MatchRejectedEvent event) {
    notificationSendService.send(
        new NotificationSendRequest(
            event.id().toString(),
            SenderType.MEMBER,
            event.responderId(),
            event.requesterId(),
            NotificationType.MATCH_REJECT,
            Map.of(NotificationSendRequest.SENDER_NAME, event.responderName()),
            ChannelType.PUSH));
  }
}
