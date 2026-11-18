package kr.co.deepple.notification.adapter.in.kafka;

import kr.co.deepple.common.event.EventConsumer;
import kr.co.deepple.member.event.MemberBecameActiveEvent;
import kr.co.deepple.member.event.MemberBecameDormantEvent;
import kr.co.deepple.member.event.MemberLoggedInEvent;
import kr.co.deepple.member.event.MemberRegisteredEvent;
import kr.co.deepple.member.event.MemberSettingUpdatedEvent;
import kr.co.deepple.member.event.MemberTopic;
import kr.co.deepple.notification.application.DeviceRegistrationService;
import kr.co.deepple.notification.application.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("notificationMemberEventConsumer")
@RequiredArgsConstructor
@KafkaListener(topics = MemberTopic.NAME, groupId = NotificationConsumerGroup.ID)
public class MemberEventConsumer extends EventConsumer {

  private final DeviceRegistrationService deviceRegistrationService;
  private final NotificationPreferenceService notificationPreferenceService;

  @KafkaHandler
  public void handle(MemberRegisteredEvent event) {
    notificationPreferenceService.createIfAbsent(event.memberId());
  }

  @KafkaHandler
  public void handle(MemberSettingUpdatedEvent event) {
    notificationPreferenceService.changeGloballyIfPresent(
        event.memberId(), event.isPushNotificationEnabled(), event.occurredAt());
  }

  @KafkaHandler
  public void handle(MemberLoggedInEvent event) {
    deviceRegistrationService.activateByMemberId(event.memberId(), event.occurredAt());
  }

  @KafkaHandler
  public void handle(MemberBecameActiveEvent event) {
    deviceRegistrationService.activateByMemberId(event.memberId(), event.occurredAt());
  }

  @KafkaHandler
  public void handle(MemberBecameDormantEvent event) {
    deviceRegistrationService.deactivateByMemberId(event.memberId(), event.occurredAt());
  }
}
