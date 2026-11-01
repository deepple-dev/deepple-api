package deepple.deepple.mission.command.infra.memberMission;

import deepple.deepple.community.command.domain.selfintroduction.event.SelfIntroductionFirstWrittenEvent;
import deepple.deepple.datingexam.application.dto.AllRequiredSubjectSubmittedEvent;
import deepple.deepple.mission.command.application.memberMission.MemberMissionService;
import deepple.deepple.mission.command.domain.mission.ActionType;
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
        memberMissionService.executeMissionsByAction(event.getMemberId(), ActionType.FIRST_DATE_EXAM.name());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SelfIntroductionFirstWrittenEvent event) {
        memberMissionService.executeMissionsByAction(event.getMemberId(), ActionType.SELF_INTRODUCTION.name());
    }
}
