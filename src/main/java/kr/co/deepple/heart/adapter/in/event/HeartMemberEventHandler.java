package kr.co.deepple.heart.adapter.in.event;

import kr.co.deepple.heart.application.port.in.SyncHeartMemberCommand;
import kr.co.deepple.heart.application.port.in.SyncHeartMemberUseCase;
import kr.co.deepple.member.command.domain.member.event.MemberSummaryChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class HeartMemberEventHandler {

  private final SyncHeartMemberUseCase syncHeartMemberUseCase;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(MemberSummaryChangedEvent event) {
    syncHeartMemberUseCase.syncHeartMember(
        new SyncHeartMemberCommand(
            event.getMemberId(), event.getGender(), event.isVip(), event.getTimestamp()));
  }
}
