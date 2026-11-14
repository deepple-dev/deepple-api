package kr.co.deepple.notification.adapter.in.event;

import kr.co.deepple.member.event.MemberSettingUpdatedEvent;
import kr.co.deepple.notification.application.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberSettingUpdatedEventHandler {

  private final NotificationPreferenceService notificationPreferenceService;

  @ApplicationModuleListener
  public void handle(MemberSettingUpdatedEvent event) {
    notificationPreferenceService.changeGloballyIfPresent(
        event.memberId(), event.isPushNotificationEnabled());
  }
}
