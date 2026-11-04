package deepple.deepple.notification.infra;

import deepple.deepple.member.command.domain.member.event.MemberBecameActiveEvent;
import deepple.deepple.member.command.domain.member.event.MemberBecameDormantEvent;
import deepple.deepple.member.command.domain.member.event.MemberLoggedInEvent;
import deepple.deepple.member.command.domain.member.event.MemberLoggedOutEvent;
import deepple.deepple.notification.command.application.DeviceRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceRegistrationEventHandler {

    private final DeviceRegistrationService deviceRegistrationService;

    @Async
    @TransactionalEventListener(value = MemberLoggedInEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberLoggedInEvent(MemberLoggedInEvent event) {
        deviceRegistrationService.activateByMemberId(event.getMemberId());
        log.info("사용자(id: {})의 디바이스 등록 활성화 (로그인)", event.getMemberId());
    }

    @Async
    @TransactionalEventListener(value = MemberLoggedOutEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberLoggedOutEvent(MemberLoggedOutEvent event) {
        deviceRegistrationService.deactivateByMemberId(event.getMemberId());
        log.info("사용자(id: {})의 디바이스 등록 비활성화 (로그아웃)", event.getMemberId());
    }

    @Async
    @TransactionalEventListener(value = MemberBecameDormantEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberBecameDormantEvent(MemberBecameDormantEvent event) {
        deviceRegistrationService.deactivateByMemberId(event.getMemberId());
        log.info("사용자(id: {})의 디바이스 등록 비활성화 (휴면)", event.getMemberId());
    }

    @Async
    @TransactionalEventListener(value = MemberBecameActiveEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberActivatedEvent(MemberBecameActiveEvent event) {
        deviceRegistrationService.activateByMemberId(event.getMemberId());
        log.info("사용자(id: {})의 디바이스 등록 활성화 (휴면 해제)", event.getMemberId());
    }
}