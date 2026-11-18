package kr.co.deepple.notification.adapter.in.kafka;

import java.util.Map;
import kr.co.deepple.admin.event.AdminTopic;
import kr.co.deepple.admin.event.ScreeningApprovedEvent;
import kr.co.deepple.admin.event.ScreeningRejectedEvent;
import kr.co.deepple.admin.event.WarningIssuedEvent;
import kr.co.deepple.common.event.EventConsumer;
import kr.co.deepple.notification.application.NotificationSendService;
import kr.co.deepple.notification.application.dto.NotificationSendRequest;
import kr.co.deepple.notification.domain.ChannelType;
import kr.co.deepple.notification.domain.NotificationType;
import kr.co.deepple.notification.domain.SenderType;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("notificationAdminEventConsumer")
@RequiredArgsConstructor
@KafkaListener(topics = AdminTopic.NAME, groupId = NotificationConsumerGroup.ID)
public class AdminEventConsumer extends EventConsumer {

  private final NotificationSendService notificationSendService;

  @KafkaHandler
  public void handle(WarningIssuedEvent event) {
    notificationSendService.send(
        new NotificationSendRequest(
            event.id().toString(),
            SenderType.ADMIN,
            event.adminId(),
            event.memberId(),
            NotificationType.valueOf(event.reasonType()),
            Map.of(),
            ChannelType.PUSH));
  }

  @KafkaHandler
  public void handle(ScreeningApprovedEvent event) {
    notificationSendService.send(
        new NotificationSendRequest(
            event.id().toString(),
            SenderType.ADMIN,
            event.adminId(),
            event.memberId(),
            NotificationType.SCREENING_APPROVED,
            Map.of(),
            ChannelType.PUSH));
  }

  @KafkaHandler
  public void handle(ScreeningRejectedEvent event) {
    notificationSendService.send(
        new NotificationSendRequest(
            event.id().toString(),
            SenderType.ADMIN,
            event.adminId(),
            event.memberId(),
            NotificationType.SCREENING_REJECTED,
            Map.of("rejectionReason", event.rejectionReason()),
            ChannelType.PUSH));
  }
}
