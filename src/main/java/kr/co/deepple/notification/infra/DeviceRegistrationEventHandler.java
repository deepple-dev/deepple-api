package kr.co.deepple.notification.infra;

import kr.co.deepple.auth.event.MemberAccountLoggedOutEvent;
import kr.co.deepple.member.event.MemberBecameActiveEvent;
import kr.co.deepple.member.event.MemberBecameDormantEvent;
import kr.co.deepple.member.event.MemberLoggedInEvent;
import kr.co.deepple.notification.command.application.DeviceRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceRegistrationEventHandler {

  private final DeviceRegistrationService deviceRegistrationService;

  @ApplicationModuleListener
  public void handleMemberLoggedInEvent(MemberLoggedInEvent event) {
    deviceRegistrationService.activateByMemberId(event.memberId());
  }

  @ApplicationModuleListener
  public void handleMemberAccountLoggedOutEvent(MemberAccountLoggedOutEvent event) {
    deviceRegistrationService.deactivateByMemberId(event.accountId());
  }

  @ApplicationModuleListener
  public void handleMemberBecameDormantEvent(MemberBecameDormantEvent event) {
    deviceRegistrationService.deactivateByMemberId(event.memberId());
  }

  @ApplicationModuleListener
  public void handleMemberActivatedEvent(MemberBecameActiveEvent event) {
    deviceRegistrationService.activateByMemberId(event.memberId());
  }
}
