package kr.co.deepple.notification.adapter.in.event;

import kr.co.deepple.member.event.MemberRegisteredEvent;
import kr.co.deepple.notification.application.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service("notificationMemberRegisteredEventHandler")
@RequiredArgsConstructor
public class MemberRegisteredEventHandler {

  private final NotificationPreferenceService notificationPreferenceService;

  @ApplicationModuleListener
  public void handle(MemberRegisteredEvent event) {
    notificationPreferenceService.createIfAbsent(event.memberId());
  }
}
