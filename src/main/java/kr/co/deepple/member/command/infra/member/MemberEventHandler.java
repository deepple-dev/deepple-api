package kr.co.deepple.member.command.infra.member;

import kr.co.deepple.datingexam.event.AllRequiredSubjectSubmittedEvent;
import kr.co.deepple.member.command.application.member.MemberProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberEventHandler {

  private final MemberProfileService memberProfileService;

  @ApplicationModuleListener
  public void handle(AllRequiredSubjectSubmittedEvent event) {
    memberProfileService.markDatingExamSubmitted(event.memberId());
  }
}
