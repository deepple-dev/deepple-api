package deepple.deepple.notification.infra;

import deepple.deepple.member.command.domain.member.event.MemberBecameActiveEvent;
import deepple.deepple.member.command.domain.member.event.MemberBecameDormantEvent;
import deepple.deepple.member.command.domain.member.event.MemberLoggedInEvent;
import deepple.deepple.member.command.domain.member.event.MemberLoggedOutEvent;
import deepple.deepple.notification.command.application.DeviceRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class DeviceRegistrationEventHandler {

    private final DeviceRegistrationService deviceRegistrationService;

    @Async
    @TransactionalEventListener(value = MemberLoggedInEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberLoggedInEvent(MemberLoggedInEvent event) {
        deviceRegistrationService.activateByMemberId(event.getMemberId());
    }

    @Async
    @TransactionalEventListener(value = MemberLoggedOutEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberLoggedOutEvent(MemberLoggedOutEvent event) {
        deviceRegistrationService.deactivateByMemberId(event.getMemberId());
    }

    @Async
    @TransactionalEventListener(value = MemberBecameDormantEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberBecameDormantEvent(MemberBecameDormantEvent event) {
        deviceRegistrationService.deactivateByMemberId(event.getMemberId());
    }

    @Async
    @TransactionalEventListener(value = MemberBecameActiveEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberActivatedEvent(MemberBecameActiveEvent event) {
        deviceRegistrationService.activateByMemberId(event.getMemberId());
    }
}