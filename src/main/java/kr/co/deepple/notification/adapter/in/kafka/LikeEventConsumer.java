package kr.co.deepple.notification.adapter.in.kafka;

import java.util.Map;
import kr.co.deepple.common.event.EventConsumer;
import kr.co.deepple.like.event.LikeSentEvent;
import kr.co.deepple.like.event.LikeTopic;
import kr.co.deepple.notification.application.NotificationSendService;
import kr.co.deepple.notification.application.port.in.SendNotificationCommand;
import kr.co.deepple.notification.domain.ChannelType;
import kr.co.deepple.notification.domain.NotificationType;
import kr.co.deepple.notification.domain.SenderType;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("notificationLikeEventConsumer")
@RequiredArgsConstructor
@KafkaListener(topics = LikeTopic.NAME, groupId = NotificationConsumerGroup.ID)
public class LikeEventConsumer extends EventConsumer {

  private final NotificationSendService notificationSendService;

  @KafkaHandler
  public void handle(LikeSentEvent event) {
    notificationSendService.send(
        new SendNotificationCommand(
            event.id().toString(),
            SenderType.MEMBER,
            event.senderId(),
            event.receiverId(),
            NotificationType.LIKE,
            Map.of(SendNotificationCommand.SENDER_NAME, event.senderName()),
            ChannelType.PUSH));
  }
}
