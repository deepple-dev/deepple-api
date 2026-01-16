package deepple.deepple.member.command.application.member;

import deepple.deepple.heart.command.domain.hearttransaction.vo.HeartAmount;
import deepple.deepple.member.command.application.member.exception.MemberNotFoundException;
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
        HeartAmount heartAmount = HeartAmount.from(amount);
        member.gainPurchaseHeart(heartAmount);
    }

    @Transactional
    public void grantMissionHearts(Long memberId, Long amount, String actionType) {
        Member member = getMemberById(memberId);
        HeartAmount heartAmount = HeartAmount.from(amount);
        member.gainMissionHeart(heartAmount, actionType);
    }

    @Transactional
    public void refundPurchasedHearts(Long memberId, Long amount) {
        Member member = getMemberById(memberId);
        HeartAmount heartAmount = HeartAmount.from(amount);
        member.refundPurchaseHeart(heartAmount);
    }

    private Member getMemberById(Long memberId) {
        return memberCommandRepository.findById(memberId)
            .orElseThrow(MemberNotFoundException::new);
    }
}
