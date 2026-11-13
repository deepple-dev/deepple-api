package kr.co.deepple.member.command.infra.member;

import kr.co.deepple.datingexam.application.dto.AllRequiredSubjectSubmittedEvent;
import kr.co.deepple.member.command.application.member.MemberProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberEventHandler {

  private final MemberProfileService memberProfileService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(AllRequiredSubjectSubmittedEvent event) {
    memberProfileService.markDatingExamSubmitted(event.memberId());
  }
}
