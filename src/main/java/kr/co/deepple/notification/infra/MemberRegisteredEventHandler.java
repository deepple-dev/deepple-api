package kr.co.deepple.notification.infra;

import kr.co.deepple.member.command.domain.member.event.MemberRegisteredEvent;
import kr.co.deepple.notification.command.application.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service("notificationMemberRegisteredEventHandler")
@RequiredArgsConstructor
public class MemberRegisteredEventHandler {

    private final NotificationPreferenceService notificationPreferenceService;

    @Async
    @TransactionalEventListener(value = MemberRegisteredEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MemberRegisteredEvent event) {
        notificationPreferenceService.createIfAbsent(event.getMemberId());
    }
}
