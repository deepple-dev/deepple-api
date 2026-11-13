package kr.co.deepple.heart.adapter.in.event;

import kr.co.deepple.heart.application.port.in.AddMissionHeartCommand;
import kr.co.deepple.heart.application.port.in.AddMissionHeartUseCase;
import kr.co.deepple.mission.event.MemberMissionCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HeartMissionEventHandler {

  private final AddMissionHeartUseCase addMissionHeartUseCase;

  @ApplicationModuleListener
  public void handle(MemberMissionCompletedEvent event) {
    addMissionHeartUseCase.addMissionHeart(
        new AddMissionHeartCommand(
            event.memberId(),
            event.rewardHeartAmount(),
            event.actionType(),
            event.memberMissionId() + ":" + event.successCount()));
  }
}
