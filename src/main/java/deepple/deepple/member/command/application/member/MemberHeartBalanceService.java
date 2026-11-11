package deepple.deepple.member.command.application.member;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.member.command.domain.MemberErrorCode;
import deepple.deepple.member.command.domain.heartorder.ProcessedHeartOrder;
import deepple.deepple.member.command.domain.heartorder.ProcessedHeartOrderCommandRepository;
import deepple.deepple.member.command.domain.heartorder.ProcessedHeartOperation;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberHeartBalanceService {
    private final MemberCommandRepository memberCommandRepository;
    private final ProcessedHeartOrderCommandRepository processedHeartOrderCommandRepository;

    @Transactional
    public void grantPurchasedHearts(long memberId, long orderId, long amount) {
        Member member = getMemberById(memberId);
        if (isProcessed(ProcessedHeartOperation.PURCHASE, orderId)) {
            return;
        }
        member.gainPurchaseHeart(amount);
        markProcessed(ProcessedHeartOperation.PURCHASE, orderId);
    }

    @Transactional
    public void grantMissionHearts(Long memberId, Long amount, String actionType) {
        Member member = getMemberById(memberId);
        member.gainMissionHeart(amount, actionType);
    }

    @Transactional
    public void refundPurchasedHearts(long memberId, long orderId, long amount) {
        Member member = getMemberById(memberId);
        if (isProcessed(ProcessedHeartOperation.REFUND, orderId)) {
            return;
        }
        member.refundPurchaseHeart(amount);
        markProcessed(ProcessedHeartOperation.REFUND, orderId);
    }

    private Member getMemberById(long memberId) {
        return memberCommandRepository.findByIdForUpdate(memberId)
            .orElseThrow(() -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
    }

    private boolean isProcessed(ProcessedHeartOperation operationType, long orderId) {
        if (!processedHeartOrderCommandRepository.existsByOperationTypeAndOrderId(operationType, orderId)) {
            return false;
        }
        log.atInfo()
            .addKeyValue("operationType", operationType)
            .addKeyValue("orderId", orderId)
            .log("이미 처리된 주문 하트 변경");
        return true;
    }

    private void markProcessed(ProcessedHeartOperation operationType, long orderId) {
        processedHeartOrderCommandRepository.save(ProcessedHeartOrder.of(operationType, orderId));
    }
}
