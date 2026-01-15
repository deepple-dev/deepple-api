package deepple.deepple.member.command.infra.member;


import deepple.deepple.datingexam.application.dto.AllRequiredSubjectSubmittedEvent;
import deepple.deepple.member.command.application.member.MemberHeartBalanceService;
import deepple.deepple.member.command.application.member.MemberProfileService;
import deepple.deepple.mission.command.domain.memberMission.event.MemberMissionCompletedEvent;
import deepple.deepple.payment.command.domain.order.event.HeartRefundedEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class MemberEventHandler {
    private final MemberProfileService memberProfileService;
    private final MemberHeartBalanceService memberHeartBalanceService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AllRequiredSubjectSubmittedEvent event) {
        memberProfileService.markDatingExamSubmitted(event.getMemberId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MemberMissionCompletedEvent event) {
        memberHeartBalanceService.grantMissionHearts(
            event.getMemberId(),
            event.getRewardHeartAmount(),
            event.getActionType()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(HeartRefundedEvent event) {
        try {
            memberHeartBalanceService.refundPurchasedHearts(event.getMemberId(), event.getAmount());
        } catch (Exception e) {
            log.error("하트 환불 처리 실패. memberId: {}, transactionId: {}",
                event.getMemberId(), event.getTransactionId(), e);
        }
    }
}
