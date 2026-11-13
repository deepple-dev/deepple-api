package kr.co.deepple.member.adapter.in.event;

import kr.co.deepple.datingexam.event.AllRequiredSubjectSubmittedEvent;
import kr.co.deepple.member.application.MemberProfileService;
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
