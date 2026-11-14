package kr.co.deepple.community.adapter.in.event;

import kr.co.deepple.community.event.SelfIntroductionFirstWrittenEvent;
import kr.co.deepple.mission.application.port.in.ExecuteMissionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SelfIntroductionFirstWrittenEventHandler {

  private static final String SELF_INTRODUCTION_MISSION_ACTION = "SELF_INTRODUCTION";

  private final ExecuteMissionUseCase executeMissionUseCase;

  @ApplicationModuleListener
  public void handle(SelfIntroductionFirstWrittenEvent event) {
    executeMissionUseCase.executeMissionsByAction(
        event.memberId(), SELF_INTRODUCTION_MISSION_ACTION);
  }
}
