package kr.co.deepple.datingexam.adapter.in.event;

import kr.co.deepple.datingexam.event.AllRequiredSubjectSubmittedEvent;
import kr.co.deepple.member.application.port.in.MarkDatingExamSubmittedUseCase;
import kr.co.deepple.mission.application.port.in.ExecuteMissionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AllRequiredSubjectSubmittedEventHandler {

  private static final String FIRST_DATE_EXAM_MISSION_ACTION = "FIRST_DATE_EXAM";

  private final MarkDatingExamSubmittedUseCase markDatingExamSubmittedUseCase;
  private final ExecuteMissionUseCase executeMissionUseCase;

  @ApplicationModuleListener
  public void markMemberDatingExamSubmitted(AllRequiredSubjectSubmittedEvent event) {
    markDatingExamSubmittedUseCase.markDatingExamSubmitted(event.memberId());
  }

  @ApplicationModuleListener
  public void executeFirstDateExamMission(AllRequiredSubjectSubmittedEvent event) {
    executeMissionUseCase.executeMissionsByAction(event.memberId(), FIRST_DATE_EXAM_MISSION_ACTION);
  }
}
