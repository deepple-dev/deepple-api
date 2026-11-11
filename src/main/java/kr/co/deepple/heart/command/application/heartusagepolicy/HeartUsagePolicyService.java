package kr.co.deepple.heart.command.application.heartusagepolicy;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.heart.command.domain.hearttransaction.HeartTransaction;
import kr.co.deepple.heart.command.domain.hearttransaction.HeartTransactionCommandRepository;
import kr.co.deepple.heart.command.domain.hearttransaction.vo.HeartBalance;
import kr.co.deepple.heart.command.domain.hearttransaction.vo.TransactionSubtype;
import kr.co.deepple.heart.command.domain.hearttransaction.vo.TransactionType;
import kr.co.deepple.heart.command.domain.heartusagepolicy.HeartUsagePolicy;
import kr.co.deepple.heart.command.domain.heartusagepolicy.HeartUsagePolicyCommandRepository;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
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
        HeartUsagePolicy heartUsagePolicy = getHeartUsagePolicy(member, transactionType);
        TransactionSubtype subtype = TransactionSubtype.valueOf(transactionSubtype);
        if (heartUsagePolicy.isFree(member.isVip(), subtype)) {
            return;
        }
        long price = heartUsagePolicy.getPrice();
        member.useHeart(price);
        createHeartTransaction(member, transactionType, content, price, member.getHeartBalance());
    }

    private Member getMember(long memberId) {
        return memberCommandRepository.findByIdForUpdate(memberId)
            .orElseThrow(() -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
    }

    private HeartUsagePolicy getHeartUsagePolicy(Member member, TransactionType transactionType) {
        return heartUsagePolicyCommandRepository.findByGenderAndTransactionType(member.getGender(), transactionType)
            .orElseThrow(() -> new IllegalStateException(
                "하트 사용 정책이 없습니다. gender=" + member.getGender() + ", transactionType=" + transactionType));
    }

    private void createHeartTransaction(Member member, TransactionType transactionType, String content,
        long price, HeartBalance balanceAfterUsingHeart) {
        HeartTransaction heartTransaction = HeartTransaction.of(member.getId(), transactionType, content, price,
            balanceAfterUsingHeart);
        heartTransactionCommandRepository.save(heartTransaction);
    }
}
