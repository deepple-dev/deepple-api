package deepple.deepple.auth.infra;

import deepple.deepple.admin.command.domain.suspension.MemberSuspendedEvent;
import deepple.deepple.auth.domain.RefreshTokenService;
import deepple.deepple.auth.domain.TokenOwner;
import deepple.deepple.member.command.domain.member.event.MemberBecameDormantEvent;
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
    @TransactionalEventListener(value = MemberSuspendedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MemberSuspendedEvent event) {
        refreshTokenService.revokeAll(TokenOwner.member(event.getMemberId()));
    }

    @Async
    @TransactionalEventListener(value = MemberBecameDormantEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MemberBecameDormantEvent event) {
        refreshTokenService.revokeAll(TokenOwner.member(event.getMemberId()));
    }
}
