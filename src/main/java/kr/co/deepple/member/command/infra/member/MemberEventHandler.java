package kr.co.deepple.member.command.infra.member;


import kr.co.deepple.datingexam.application.dto.AllRequiredSubjectSubmittedEvent;
import kr.co.deepple.member.command.application.member.MemberHeartBalanceService;
import kr.co.deepple.member.command.application.member.MemberProfileService;
import kr.co.deepple.mission.command.domain.memberMission.event.MemberMissionCompletedEvent;
import kr.co.deepple.payment.command.domain.order.event.HeartRefundedEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
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
        memberHeartBalanceService.refundPurchasedHearts(event.getMemberId(), event.getOrderId(), event.getAmount());
    }
}
