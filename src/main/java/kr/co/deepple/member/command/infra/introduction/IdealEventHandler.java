package kr.co.deepple.member.command.infra.introduction;

import kr.co.deepple.member.command.application.introduction.MemberIdealService;
import kr.co.deepple.member.event.MemberRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class IdealEventHandler {

  private final MemberIdealService memberIdealService;

  @Async
  @TransactionalEventListener(
      value = MemberRegisteredEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handle(MemberRegisteredEvent event) {
    memberIdealService.initIfAbsent(event.memberId());
  }
}
