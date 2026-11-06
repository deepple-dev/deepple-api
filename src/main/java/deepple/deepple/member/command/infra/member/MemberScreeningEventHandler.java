package deepple.deepple.member.command.infra.member;

import deepple.deepple.admin.command.domain.screening.event.ScreeningApprovedEvent;
import deepple.deepple.admin.command.domain.screening.event.ScreeningCreatedEvent;
import deepple.deepple.admin.command.domain.screening.event.ScreeningRejectedEvent;
import deepple.deepple.member.command.application.member.MemberProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class MemberScreeningEventHandler {
    private final MemberProfileService memberProfileService;

    @Async
    @TransactionalEventListener(value = ScreeningApprovedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ScreeningApprovedEvent event) {
        memberProfileService.approveScreening(event.getMemberId());
    }

    @Async
    @TransactionalEventListener(value = ScreeningRejectedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ScreeningRejectedEvent event) {
        memberProfileService.changeReject(event.getMemberId());
    }

    @Async
    @TransactionalEventListener(value = ScreeningCreatedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ScreeningCreatedEvent event) {
        memberProfileService.changeWaiting(event.getMemberId());
    }
}
