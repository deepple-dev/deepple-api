package deepple.deepple.heart.command.application.hearttransaction;

import deepple.deepple.heart.command.domain.hearttransaction.HeartTransactionCommandRepository;
import deepple.deepple.heart.command.domain.hearttransaction.vo.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HeartTransactionServiceTest {
    @Mock
    private HeartTransactionCommandRepository heartTransactionCommandRepository;

    @InjectMocks
    private HeartTransactionService heartTransactionService;

    @Test
    @DisplayName("하트 구매 트랜잭션을 생성합니다.")
    void createHeartPurchaseTransaction() {
        // given
        Long memberId = 1L;
        Long amount = 100L;
        Long missionHeartBalance = 100L;
        Long purchaseHeartBalance = 100L;

        // when
        heartTransactionService.createHeartPurchaseTransaction(memberId, amount, missionHeartBalance,
            purchaseHeartBalance);

        // then
        verify(heartTransactionCommandRepository).save(argThat(heartTransaction ->
            heartTransaction.getMemberId().equals(memberId) &&
                heartTransaction.getTransactionType() == TransactionType.PURCHASE &&
                heartTransaction.getHeartAmount().getAmount().equals(amount) &&
                heartTransaction.getHeartBalance().getMissionHeartBalance().equals(missionHeartBalance) &&
                heartTransaction.getHeartBalance().getPurchaseHeartBalance().equals(purchaseHeartBalance)
        ));
    }

    @Test
    @DisplayName("하트 미션 트랜잭션을 생성합니다.")
    void createHeartMissionTransaction() {
        // given
        Long memberId = 1L;
        Long amount = 50L;
        Long missionHeartBalance = 150L;
        Long purchaseHeartBalance = 100L;
        String actionType = "Daily Login";

        // when
        heartTransactionService.createHeartMissionTransaction(memberId, amount, missionHeartBalance,
            purchaseHeartBalance, actionType);

        // then
        verify(heartTransactionCommandRepository).save(argThat(heartTransaction ->
            heartTransaction.getMemberId().equals(memberId) &&
                heartTransaction.getTransactionType() == TransactionType.MISSION &&
                heartTransaction.getContent().equals(actionType) &&
                heartTransaction.getHeartAmount().getAmount().equals(amount) &&
                heartTransaction.getHeartBalance().getMissionHeartBalance().equals(missionHeartBalance) &&
                heartTransaction.getHeartBalance().getPurchaseHeartBalance().equals(purchaseHeartBalance)
        ));
    }

    @Test
    @DisplayName("관리자 지급 트랜잭션을 생성합니다.")
    void createAdminGrantTransaction() {
        // given
        Long memberId = 1L;
        Long amount = 30L;
        Long missionHeartBalance = 130L;
        Long purchaseHeartBalance = 50L;
        String reason = "CS 보상";

        // when
        heartTransactionService.createAdminGrantTransaction(memberId, amount, missionHeartBalance,
            purchaseHeartBalance, reason);

        // then
        verify(heartTransactionCommandRepository).save(argThat(heartTransaction ->
            heartTransaction.getMemberId().equals(memberId) &&
                heartTransaction.getTransactionType() == TransactionType.ADMIN_GRANT &&
                heartTransaction.getContent().equals(reason) &&
                heartTransaction.getHeartAmount().getAmount().equals(amount) &&
                heartTransaction.getHeartBalance().getMissionHeartBalance().equals(missionHeartBalance) &&
                heartTransaction.getHeartBalance().getPurchaseHeartBalance().equals(purchaseHeartBalance)
        ));
    }
}