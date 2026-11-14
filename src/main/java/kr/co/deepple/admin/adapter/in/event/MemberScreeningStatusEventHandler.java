package kr.co.deepple.admin.adapter.in.event;

import kr.co.deepple.admin.event.ScreeningApprovedEvent;
import kr.co.deepple.admin.event.ScreeningCreatedEvent;
import kr.co.deepple.admin.event.ScreeningRejectedEvent;
import kr.co.deepple.member.application.port.in.ChangeMemberScreeningStatusUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberScreeningStatusEventHandler {

  private final ChangeMemberScreeningStatusUseCase changeMemberScreeningStatusUseCase;

  @ApplicationModuleListener
  public void handle(ScreeningApprovedEvent event) {
    changeMemberScreeningStatusUseCase.approveScreening(event.memberId());
  }

  @ApplicationModuleListener
  public void handle(ScreeningRejectedEvent event) {
    changeMemberScreeningStatusUseCase.changeReject(event.memberId());
  }

  @ApplicationModuleListener
  public void handle(ScreeningCreatedEvent event) {
    changeMemberScreeningStatusUseCase.changeWaiting(event.memberId());
  }
}
