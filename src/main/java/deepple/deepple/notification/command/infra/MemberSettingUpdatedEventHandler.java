package deepple.deepple.notification.command.infra;

import deepple.deepple.member.command.domain.member.event.MemberSettingUpdatedEvent;
import deepple.deepple.notification.command.application.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberSettingUpdatedEventHandler {
    private final NotificationPreferenceService notificationPreferenceService;

    @Async
    @TransactionalEventListener(value = MemberSettingUpdatedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MemberSettingUpdatedEvent event) {
        try {
            notificationPreferenceService.changeGloballyIfPresent(
                event.getMemberId(),
                event.isPushNotificationEnabled()
            );
        } catch (Exception e) {
            log.error("Member(id: {})의 NotificationPreference 업데이트 중 예외가 발생했습니다.", event.getMemberId(), e);
        }
    }
}
