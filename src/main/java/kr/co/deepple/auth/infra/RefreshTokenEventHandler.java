package kr.co.deepple.auth.infra;

import kr.co.deepple.admin.event.MemberSuspendedEvent;
import kr.co.deepple.auth.domain.RefreshTokenService;
import kr.co.deepple.auth.domain.TokenOwner;
import kr.co.deepple.member.event.MemberBecameDormantEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RefreshTokenEventHandler {

  private final RefreshTokenService refreshTokenService;

  @Async
  @TransactionalEventListener(
      value = MemberSuspendedEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handle(MemberSuspendedEvent event) {
    refreshTokenService.revokeAll(TokenOwner.member(event.memberId()));
  }

  @Async
  @TransactionalEventListener(
      value = MemberBecameDormantEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handle(MemberBecameDormantEvent event) {
    refreshTokenService.revokeAll(TokenOwner.member(event.memberId()));
  }
}
