package kr.co.deepple.community.adapter.in.event;

import static org.mockito.Mockito.verify;

import kr.co.deepple.community.event.SelfIntroductionFirstWrittenEvent;
import kr.co.deepple.mission.application.port.in.ExecuteMissionUseCase;
import kr.co.deepple.mission.domain.ActionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SelfIntroductionFirstWrittenEventHandlerTest {

  private static final long MEMBER_ID = 42L;

  @Mock private ExecuteMissionUseCase executeMissionUseCase;

  @InjectMocks private SelfIntroductionFirstWrittenEventHandler handler;

  @Test
  @DisplayName("셀프소개를 처음 쓰면 셀프소개 미션을 수행한다.")
  void executeSelfIntroductionMission() {
    handler.handle(SelfIntroductionFirstWrittenEvent.from(MEMBER_ID));

    verify(executeMissionUseCase)
        .executeMissionsByAction(MEMBER_ID, ActionType.SELF_INTRODUCTION.name());
  }
}
