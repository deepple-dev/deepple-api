package kr.co.deepple.match.command.infra;

import kr.co.deepple.match.command.application.match.MatchService;
import kr.co.deepple.member.event.MemberProfileRetrievedEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchEventHandler {

  private final MatchService matchService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(MemberProfileRetrievedEvent event) {
    if (event.matchRequesterId() == null || event.matchResponderId() == null) {
      return;
    }
    matchService.read(event.retrieverId(), event.matchRequesterId(), event.matchResponderId());
  }
}
