package kr.co.deepple.heart.adapter.in.event;

import kr.co.deepple.heart.application.port.in.AddMissionHeartCommand;
import kr.co.deepple.heart.application.port.in.AddMissionHeartUseCase;
import kr.co.deepple.mission.command.domain.memberMission.event.MemberMissionCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class HeartMissionEventHandler {

  private final AddMissionHeartUseCase addMissionHeartUseCase;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(MemberMissionCompletedEvent event) {
    addMissionHeartUseCase.addMissionHeart(
        new AddMissionHeartCommand(
            event.memberId(),
            event.rewardHeartAmount(),
            event.actionType(),
            event.memberMissionId() + ":" + event.successCount()));
  }
}
