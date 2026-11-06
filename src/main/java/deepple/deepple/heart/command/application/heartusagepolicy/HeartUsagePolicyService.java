package deepple.deepple.heart.command.application.heartusagepolicy;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.heart.command.domain.hearttransaction.HeartTransaction;
import deepple.deepple.heart.command.domain.hearttransaction.HeartTransactionCommandRepository;
import deepple.deepple.heart.command.domain.hearttransaction.vo.HeartAmount;
import deepple.deepple.heart.command.domain.hearttransaction.vo.HeartBalance;
import deepple.deepple.heart.command.domain.hearttransaction.vo.TransactionSubtype;
import deepple.deepple.heart.command.domain.hearttransaction.vo.TransactionType;
import deepple.deepple.heart.command.domain.heartusagepolicy.HeartUsagePolicy;
import deepple.deepple.heart.command.domain.heartusagepolicy.HeartUsagePolicyCommandRepository;
import deepple.deepple.member.command.domain.MemberErrorCode;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HeartUsagePolicyService {
    private final HeartUsagePolicyCommandRepository heartUsagePolicyCommandRepository;
    private final HeartTransactionCommandRepository heartTransactionCommandRepository;
    private final MemberCommandRepository memberCommandRepository;

    @Transactional
    public void useHeart(long memberId, TransactionType transactionType, String content,
        String transactionSubtype) {
        Member member = getMember(memberId);
        HeartAmount heartAmount = getHeartAmount(member, transactionType, transactionSubtype);
        if (heartAmount.isZero()) {
            return;
        }
        HeartBalance balanceAfterUsingHeart = deductHeartBalance(member, heartAmount);
        createHeartTransaction(member, transactionType, content, heartAmount, balanceAfterUsingHeart);
    }

    private Member getMember(long memberId) {
        return memberCommandRepository.findById(memberId)
            .orElseThrow(() -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
    }

    private HeartAmount getHeartAmount(Member member, TransactionType transactionType, String transactionSubtype) {
        HeartUsagePolicy heartUsagePolicy = heartUsagePolicyCommandRepository.findByGenderAndTransactionType(
                member.getGender(), transactionType)
            .orElseThrow(() -> new IllegalStateException(
                "하트 사용 정책이 없습니다. gender=" + member.getGender() + ", transactionType=" + transactionType));
        TransactionSubtype subtype = TransactionSubtype.valueOf(transactionSubtype);
        return HeartAmount.from(heartUsagePolicy.getAmount(member.isVip(), subtype));
    }

    private HeartBalance deductHeartBalance(Member member, HeartAmount heartAmount) {
        member.useHeart(heartAmount);
        HeartBalance balanceAfterUsingHeart = member.getHeartBalance();
        return balanceAfterUsingHeart;
    }

    private void createHeartTransaction(Member member, TransactionType transactionType, String content,
        HeartAmount heartAmount, HeartBalance balanceAfterUsingHeart) {
        HeartTransaction heartTransaction = HeartTransaction.of(member.getId(), transactionType, content, heartAmount,
            balanceAfterUsingHeart);
        heartTransactionCommandRepository.save(heartTransaction);
    }
}
