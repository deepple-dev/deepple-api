package kr.co.deepple.notification.adapter.in.kafka;

import java.util.Map;
import kr.co.deepple.common.event.EventConsumer;
import kr.co.deepple.community.event.CommunityTopic;
import kr.co.deepple.community.event.ProfileExchangeAcceptedEvent;
import kr.co.deepple.community.event.ProfileExchangeRejectedEvent;
import kr.co.deepple.community.event.ProfileExchangeRequestedEvent;
import kr.co.deepple.notification.application.NotificationSendService;
import kr.co.deepple.notification.application.dto.NotificationSendRequest;
import kr.co.deepple.notification.domain.ChannelType;
import kr.co.deepple.notification.domain.NotificationType;
import kr.co.deepple.notification.domain.SenderType;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("notificationCommunityEventConsumer")
@RequiredArgsConstructor
@KafkaListener(topics = CommunityTopic.NAME, groupId = NotificationConsumerGroup.ID)
public class CommunityEventConsumer extends EventConsumer {

  private final NotificationSendService notificationSendService;

  @KafkaHandler
  public void handle(ProfileExchangeRequestedEvent event) {
    notificationSendService.send(
        new NotificationSendRequest(
            event.id().toString(),
            SenderType.MEMBER,
            event.requesterId(),
            event.responderId(),
            NotificationType.PROFILE_EXCHANGE_REQUEST,
            Map.of(NotificationSendRequest.SENDER_NAME, event.senderName()),
            ChannelType.PUSH));
  }

  @KafkaHandler
  public void handle(ProfileExchangeAcceptedEvent event) {
    notificationSendService.send(
        new NotificationSendRequest(
            event.id().toString(),
            SenderType.MEMBER,
            event.responderId(),
            event.requesterId(),
            NotificationType.PROFILE_EXCHANGE_ACCEPT,
            Map.of(NotificationSendRequest.SENDER_NAME, event.senderName()),
            ChannelType.PUSH));
  }

  @KafkaHandler
  public void handle(ProfileExchangeRejectedEvent event) {
    notificationSendService.send(
        new NotificationSendRequest(
            event.id().toString(),
            SenderType.MEMBER,
            event.responderId(),
            event.requesterId(),
            NotificationType.PROFILE_EXCHANGE_REJECT,
            Map.of(NotificationSendRequest.SENDER_NAME, event.senderName()),
            ChannelType.PUSH));
  }
}
