package kr.co.deepple.notification.adapter.in.kafka;

import java.util.Map;
import kr.co.deepple.common.event.EventConsumer;
import kr.co.deepple.heart.event.AdminHeartGrantedEvent;
import kr.co.deepple.heart.event.HeartTopic;
import kr.co.deepple.notification.application.NotificationSendService;
import kr.co.deepple.notification.application.port.in.SendNotificationCommand;
import kr.co.deepple.notification.domain.ChannelType;
import kr.co.deepple.notification.domain.NotificationType;
import kr.co.deepple.notification.domain.SenderType;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("notificationHeartEventConsumer")
@RequiredArgsConstructor
@KafkaListener(topics = HeartTopic.NAME, groupId = NotificationConsumerGroup.ID)
public class HeartEventConsumer extends EventConsumer {

  private final NotificationSendService notificationSendService;

  @KafkaHandler
  public void handle(AdminHeartGrantedEvent event) {
    notificationSendService.send(
        new SendNotificationCommand(
            event.id().toString(),
            SenderType.ADMIN,
            event.adminId(),
            event.memberId(),
            NotificationType.HEART_GRANTED,
            Map.of("amount", String.valueOf(event.amount())),
            ChannelType.PUSH));
  }
}
