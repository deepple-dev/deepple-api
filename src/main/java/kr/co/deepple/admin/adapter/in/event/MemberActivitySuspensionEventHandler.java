package kr.co.deepple.admin.adapter.in.event;

import kr.co.deepple.admin.event.MemberSuspendedEvent;
import kr.co.deepple.admin.event.MemberUnsuspendedEvent;
import kr.co.deepple.member.application.port.in.ChangeMemberActivityStatusUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberActivitySuspensionEventHandler {

  private static final String ACTIVE_STATUS = "ACTIVE";

  private final ChangeMemberActivityStatusUseCase changeMemberActivityStatusUseCase;

  @ApplicationModuleListener
  public void handle(MemberSuspendedEvent event) {
    changeMemberActivityStatusUseCase.changeMemberActivityStatus(event.memberId(), event.status());
  }

  @ApplicationModuleListener
  public void handle(MemberUnsuspendedEvent event) {
    changeMemberActivityStatusUseCase.changeMemberActivityStatus(event.memberId(), ACTIVE_STATUS);
  }
}
