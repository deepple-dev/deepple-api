package deepple.deepple.admin.command.infra.screening;

import deepple.deepple.admin.command.application.screening.ScreeningService;
import deepple.deepple.member.command.domain.member.event.MemberProfileInitializedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class ScreeningEventHandler {
    private final ScreeningService screeningService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MemberProfileInitializedEvent event) {
        screeningService.createIfAbsent(event.getMemberId());
    }
}
