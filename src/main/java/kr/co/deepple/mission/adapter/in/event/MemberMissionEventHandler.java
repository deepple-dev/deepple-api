package kr.co.deepple.mission.adapter.in.event;

import kr.co.deepple.community.event.SelfIntroductionFirstWrittenEvent;
import kr.co.deepple.datingexam.event.AllRequiredSubjectSubmittedEvent;
import kr.co.deepple.mission.application.MemberMissionService;
import kr.co.deepple.mission.domain.ActionType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberMissionEventHandler {

  private final MemberMissionService memberMissionService;

  @ApplicationModuleListener
  public void handle(AllRequiredSubjectSubmittedEvent event) {
    memberMissionService.executeMissionsByAction(
        event.memberId(), ActionType.FIRST_DATE_EXAM.name());
  }

  @ApplicationModuleListener
  public void handle(SelfIntroductionFirstWrittenEvent event) {
    memberMissionService.executeMissionsByAction(
        event.memberId(), ActionType.SELF_INTRODUCTION.name());
  }
}
