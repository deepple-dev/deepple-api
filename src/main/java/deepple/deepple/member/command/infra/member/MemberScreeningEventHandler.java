package deepple.deepple.member.command.infra.member;

import deepple.deepple.admin.command.domain.screening.event.ScreeningApprovedEvent;
import deepple.deepple.admin.command.domain.screening.event.ScreeningCreatedEvent;
import deepple.deepple.admin.command.domain.screening.event.ScreeningRejectedEvent;
import deepple.deepple.member.command.application.member.MemberProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberScreeningEventHandler {
    private final MemberProfileService memberProfileService;

    @Async
    @TransactionalEventListener(value = ScreeningApprovedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ScreeningApprovedEvent event) {
        try {
            memberProfileService.approveScreening(event.getMemberId());
        } catch (Exception e) {
            log.error("Member(id: {})의 프로필 업데이트 중 예외가 발생했습니다.", event.getMemberId(), e);
        }
    }

    @Async
    @TransactionalEventListener(value = ScreeningRejectedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ScreeningRejectedEvent event) {
        try {
            memberProfileService.changeReject(event.getMemberId());
        } catch (Exception e) {
            log.error("Member(id: {})의 프로필 업데이트 중 예외가 발생했습니다.", event.getMemberId(), e);
        }
    }

    @Async
    @TransactionalEventListener(value = ScreeningCreatedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ScreeningCreatedEvent event) {
        try {
            memberProfileService.changeWaiting(event.getMemberId());
        } catch (Exception e) {
            log.error("Member(id: {})의 프로필 업데이트 중 예외가 발생했습니다.", event.getMemberId(), e);
        }
    }
}
