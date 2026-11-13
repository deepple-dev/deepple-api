package kr.co.deepple.heart.adapter.in.event;

import kr.co.deepple.heart.application.port.in.SyncHeartMemberCommand;
import kr.co.deepple.heart.application.port.in.SyncHeartMemberUseCase;
import kr.co.deepple.member.event.MemberSummaryChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HeartMemberEventHandler {

  private final SyncHeartMemberUseCase syncHeartMemberUseCase;

  @ApplicationModuleListener
  public void handle(MemberSummaryChangedEvent event) {
    syncHeartMemberUseCase.syncHeartMember(
        new SyncHeartMemberCommand(
            event.memberId(), event.gender(), event.isVip(), event.occurredAt()));
  }
}
