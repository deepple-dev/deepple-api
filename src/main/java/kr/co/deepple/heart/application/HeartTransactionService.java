package kr.co.deepple.heart.application;

import kr.co.deepple.heart.application.port.out.HeartTransactionCommandRepository;
import kr.co.deepple.heart.domain.HeartBalance;
import kr.co.deepple.heart.domain.HeartTransaction;
import kr.co.deepple.heart.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HeartTransactionService {
    private final HeartTransactionCommandRepository heartTransactionCommandRepository;

    @Transactional
    public void createHeartPurchaseTransaction(Long memberId, Long amount, Long missionHeartBalance,
        Long purchaseHeartBalance) {
        createHeartTransaction(memberId, amount, missionHeartBalance, purchaseHeartBalance, TransactionType.PURCHASE);
    }

    private void createHeartTransaction(Long memberId, Long amount, Long missionHeartBalance, Long purchaseHeartBalance,
        TransactionType transactionType) {
        HeartBalance heartBalance = HeartBalance.of(missionHeartBalance, purchaseHeartBalance);
        HeartTransaction heartTransaction = HeartTransaction.of(memberId, transactionType,
            transactionType.getDescription(), amount, heartBalance);
        heartTransactionCommandRepository.save(heartTransaction);
    }

    @Transactional
    public void createHeartMissionTransaction(Long memberId, Long amount, Long missionHeartBalance,
        Long purchaseHeartBalance, String actionType) {
        HeartBalance heartBalance = HeartBalance.of(missionHeartBalance, purchaseHeartBalance);
        HeartTransaction heartTransaction = HeartTransaction.of(memberId, TransactionType.MISSION,
            actionType, amount, heartBalance);
        heartTransactionCommandRepository.save(heartTransaction);
    }

    @Transactional
    public void createAdminGrantTransaction(Long memberId, Long amount, Long missionHeartBalance,
        Long purchaseHeartBalance, String reason) {
        HeartBalance heartBalance = HeartBalance.of(missionHeartBalance, purchaseHeartBalance);
        HeartTransaction heartTransaction = HeartTransaction.of(memberId, TransactionType.ADMIN_GRANT,
            reason, amount, heartBalance);
        heartTransactionCommandRepository.save(heartTransaction);
    }
}
