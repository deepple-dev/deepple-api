package kr.co.deepple.member.command.infra.member;

import kr.co.deepple.admin.event.ScreeningApprovedEvent;
import kr.co.deepple.admin.event.ScreeningCreatedEvent;
import kr.co.deepple.admin.event.ScreeningRejectedEvent;
import kr.co.deepple.member.command.application.member.MemberProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberScreeningEventHandler {

  private final MemberProfileService memberProfileService;

  @ApplicationModuleListener
  public void handle(ScreeningApprovedEvent event) {
    memberProfileService.approveScreening(event.memberId());
  }

  @ApplicationModuleListener
  public void handle(ScreeningRejectedEvent event) {
    memberProfileService.changeReject(event.memberId());
  }

  @ApplicationModuleListener
  public void handle(ScreeningCreatedEvent event) {
    memberProfileService.changeWaiting(event.memberId());
  }
}
