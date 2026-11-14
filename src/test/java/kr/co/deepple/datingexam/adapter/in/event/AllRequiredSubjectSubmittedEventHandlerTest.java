package kr.co.deepple.datingexam.adapter.in.event;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import kr.co.deepple.datingexam.event.AllRequiredSubjectSubmittedEvent;
import kr.co.deepple.member.application.port.in.MarkDatingExamSubmittedUseCase;
import kr.co.deepple.mission.application.port.in.ExecuteMissionUseCase;
import kr.co.deepple.mission.domain.ActionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AllRequiredSubjectSubmittedEventHandlerTest {

  private static final long MEMBER_ID = 42L;

  @Mock private MarkDatingExamSubmittedUseCase markDatingExamSubmittedUseCase;

  @Mock private ExecuteMissionUseCase executeMissionUseCase;

  @InjectMocks private AllRequiredSubjectSubmittedEventHandler handler;

  @Test
  @DisplayName("필수 과목을 모두 제출하면 회원에 연애고사 제출을 표시한다.")
  void markMemberDatingExamSubmitted() {
    handler.markMemberDatingExamSubmitted(AllRequiredSubjectSubmittedEvent.from(MEMBER_ID));

    verify(markDatingExamSubmittedUseCase).markDatingExamSubmitted(MEMBER_ID);
    verifyNoInteractions(executeMissionUseCase);
  }

  @Test
  @DisplayName("필수 과목을 모두 제출하면 연애고사 미션을 수행한다.")
  void executeFirstDateExamMission() {
    handler.executeFirstDateExamMission(AllRequiredSubjectSubmittedEvent.from(MEMBER_ID));

    verify(executeMissionUseCase)
        .executeMissionsByAction(MEMBER_ID, ActionType.FIRST_DATE_EXAM.name());
    verifyNoInteractions(markDatingExamSubmittedUseCase);
  }
}
