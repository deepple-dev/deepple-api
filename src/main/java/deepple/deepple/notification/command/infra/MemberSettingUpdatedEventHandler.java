package deepple.deepple.notification.command.infra;

import deepple.deepple.member.command.domain.member.event.MemberSettingUpdatedEvent;
import deepple.deepple.notification.command.application.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class MemberSettingUpdatedEventHandler {
    private final NotificationPreferenceService notificationPreferenceService;

    @Async
    @TransactionalEventListener(value = MemberSettingUpdatedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MemberSettingUpdatedEvent event) {
        notificationPreferenceService.changeGloballyIfPresent(
            event.getMemberId(),
            event.isPushNotificationEnabled()
        );
    }
}
