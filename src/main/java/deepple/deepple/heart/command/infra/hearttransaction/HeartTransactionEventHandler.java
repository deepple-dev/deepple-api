package deepple.deepple.heart.command.infra.hearttransaction;

import deepple.deepple.heart.command.application.hearttransaction.HeartTransactionService;
import deepple.deepple.member.command.domain.member.event.AdminHeartGrantedEvent;
import deepple.deepple.member.command.domain.member.event.MissionHeartGainedEvent;
import deepple.deepple.member.command.domain.member.event.PurchaseHeartGainedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class HeartTransactionEventHandler {
    private final HeartTransactionService heartTransactionService;

    @Async
    @TransactionalEventListener(value = PurchaseHeartGainedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PurchaseHeartGainedEvent event) {
        heartTransactionService.createHeartPurchaseTransaction(event.getMemberId(), event.getAmount(),
            event.getMissionHeartBalance(), event.getPurchaseHeartBalance());
    }

    @Async
    @TransactionalEventListener(value = MissionHeartGainedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MissionHeartGainedEvent event) {
        heartTransactionService.createHeartMissionTransaction(event.getMemberId(), event.getAmount(),
            event.getMissionHeartBalance(), event.getPurchaseHeartBalance(), event.getActionType());
    }

    @Async
    @TransactionalEventListener(value = AdminHeartGrantedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AdminHeartGrantedEvent event) {
        heartTransactionService.createAdminGrantTransaction(event.getMemberId(), event.getAmount(),
            event.getMissionHeartBalance(), event.getPurchaseHeartBalance(), event.getReason());
    }
}
