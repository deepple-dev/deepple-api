package deepple.deepple.member.command.application.member;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.member.command.domain.MemberErrorCode;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberHeartBalanceService {
    private final MemberCommandRepository memberCommandRepository;

    @Transactional
    public void grantPurchasedHearts(Long memberId, Long amount) {
        Member member = getMemberById(memberId);
        member.gainPurchaseHeart(amount);
    }

    @Transactional
    public void grantMissionHearts(Long memberId, Long amount, String actionType) {
        Member member = getMemberById(memberId);
        member.gainMissionHeart(amount, actionType);
    }

    @Transactional
    public void refundPurchasedHearts(Long memberId, Long amount) {
        Member member = getMemberById(memberId);
        member.refundPurchaseHeart(amount);
    }

    private Member getMemberById(Long memberId) {
        return memberCommandRepository.findByIdForUpdate(memberId)
            .orElseThrow(() -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
    }
}
