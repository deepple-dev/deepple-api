package deepple.deepple.member.command.infra.member;

import deepple.deepple.member.command.application.member.MemberHeartBalanceService;
import deepple.deepple.payment.command.domain.order.event.HeartPurchasedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class HeartPurchasedEventHandler {
    private final MemberHeartBalanceService memberHeartBalanceService;

    @Async
    @TransactionalEventListener(value = HeartPurchasedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handle(HeartPurchasedEvent event) {
        memberHeartBalanceService.grantPurchasedHearts(event.getMemberId(), event.getAmount());
    }
}
