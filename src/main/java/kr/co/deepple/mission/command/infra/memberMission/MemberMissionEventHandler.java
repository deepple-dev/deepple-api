package kr.co.deepple.mission.command.infra.memberMission;

import kr.co.deepple.community.command.domain.selfintroduction.event.SelfIntroductionFirstWrittenEvent;
import kr.co.deepple.datingexam.application.dto.AllRequiredSubjectSubmittedEvent;
import kr.co.deepple.mission.command.application.memberMission.MemberMissionService;
import kr.co.deepple.mission.command.domain.mission.ActionType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberMissionEventHandler {

  private final MemberMissionService memberMissionService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(AllRequiredSubjectSubmittedEvent event) {
    memberMissionService.executeMissionsByAction(
        event.memberId(), ActionType.FIRST_DATE_EXAM.name());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(SelfIntroductionFirstWrittenEvent event) {
    memberMissionService.executeMissionsByAction(
        event.memberId(), ActionType.SELF_INTRODUCTION.name());
  }
}
