package deepple.deepple.auth.infra;

import deepple.deepple.admin.command.domain.suspension.MemberSuspendedEvent;
import deepple.deepple.auth.domain.RefreshTokenService;
import deepple.deepple.auth.domain.TokenOwner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenEventHandler {

    private final RefreshTokenService refreshTokenService;

    @Async
    @TransactionalEventListener(value = MemberSuspendedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MemberSuspendedEvent event) {
        try {
            refreshTokenService.revokeAll(TokenOwner.member(event.getMemberId()));
        } catch (Exception e) {
            log.error("정지된 회원(id: {})의 리프레시 토큰 폐기 중 예외가 발생했습니다.", event.getMemberId(), e);
        }
    }
}
