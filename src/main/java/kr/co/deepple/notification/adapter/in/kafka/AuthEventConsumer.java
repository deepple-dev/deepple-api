package kr.co.deepple.notification.adapter.in.kafka;

import kr.co.deepple.auth.event.AuthTopic;
import kr.co.deepple.auth.event.MemberAccountLoggedOutEvent;
import kr.co.deepple.common.event.EventConsumer;
import kr.co.deepple.notification.application.DeviceRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("notificationAuthEventConsumer")
@RequiredArgsConstructor
@KafkaListener(topics = AuthTopic.NAME, groupId = NotificationConsumerGroup.ID)
public class AuthEventConsumer extends EventConsumer {

  private final DeviceRegistrationService deviceRegistrationService;

  @KafkaHandler
  public void handle(MemberAccountLoggedOutEvent event) {
    deviceRegistrationService.deactivateByMemberId(event.accountId(), event.occurredAt());
  }
}
