package kr.co.deepple.member.command.infra.member;

import kr.co.deepple.admin.event.MemberSuspendedEvent;
import kr.co.deepple.admin.event.MemberUnsuspendedEvent;
import kr.co.deepple.member.command.application.member.MemberProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SuspendedEventHandler {

  private final MemberProfileService memberProfileService;

  @ApplicationModuleListener
  public void handle(MemberSuspendedEvent event) {
    memberProfileService.changeMemberActivityStatus(event.memberId(), event.status());
  }

  @ApplicationModuleListener
  public void handle(MemberUnsuspendedEvent event) {
    memberProfileService.changeMemberActivityStatus(event.memberId(), "ACTIVE");
  }
}
