package deepple.deepple.heart.command.application.hearttransaction;

import deepple.deepple.heart.command.domain.hearttransaction.HeartTransaction;
import deepple.deepple.heart.command.domain.hearttransaction.HeartTransactionCommandRepository;
import deepple.deepple.heart.command.domain.hearttransaction.vo.HeartAmount;
import deepple.deepple.heart.command.domain.hearttransaction.vo.HeartBalance;
import deepple.deepple.heart.command.domain.hearttransaction.vo.TransactionType;
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
        HeartAmount heartAmount = HeartAmount.from(amount);
        HeartTransaction heartTransaction = HeartTransaction.of(memberId, transactionType,
            transactionType.getDescription(), heartAmount, heartBalance);
        heartTransactionCommandRepository.save(heartTransaction);
    }

    @Transactional
    public void createHeartMissionTransaction(Long memberId, Long amount, Long missionHeartBalance,
        Long purchaseHeartBalance, String actionType) {
        HeartBalance heartBalance = HeartBalance.of(missionHeartBalance, purchaseHeartBalance);
        HeartAmount heartAmount = HeartAmount.from(amount);
        HeartTransaction heartTransaction = HeartTransaction.of(memberId, TransactionType.MISSION,
            actionType, heartAmount, heartBalance);
        heartTransactionCommandRepository.save(heartTransaction);
    }

    @Transactional
    public void createAdminGrantTransaction(Long memberId, Long amount, Long missionHeartBalance,
        Long purchaseHeartBalance, String reason) {
        HeartBalance heartBalance = HeartBalance.of(missionHeartBalance, purchaseHeartBalance);
        HeartAmount heartAmount = HeartAmount.from(amount);
        HeartTransaction heartTransaction = HeartTransaction.of(memberId, TransactionType.ADMIN_GRANT,
            reason, heartAmount, heartBalance);
        heartTransactionCommandRepository.save(heartTransaction);
    }
}
