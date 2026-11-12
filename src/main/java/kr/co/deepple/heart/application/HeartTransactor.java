package kr.co.deepple.heart.application;

import kr.co.deepple.heart.application.port.out.HeartTransactionRepository;
import kr.co.deepple.heart.application.port.out.MemberHeartBalanceRepository;
import kr.co.deepple.heart.domain.HeartBalance;
import kr.co.deepple.heart.domain.HeartTransaction;
import kr.co.deepple.heart.domain.MemberHeartBalance;
import kr.co.deepple.heart.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
class HeartTransactor {

  private final MemberHeartBalanceRepository memberHeartBalanceRepository;
  private final HeartTransactionRepository heartTransactionRepository;

  @Transactional(propagation = Propagation.MANDATORY)
  public boolean transact(
      long memberId,
      TransactionType transactionType,
      String content,
      long amount,
      String idempotencyKey) {
    MemberHeartBalance balance =
        memberHeartBalanceRepository
            .findByMemberIdForUpdate(memberId)
            .orElseThrow(() -> new IllegalStateException("하트 잔액이 없습니다. memberId=" + memberId));
    HeartBalance changedBalance = balance.getBalance().apply(transactionType, amount);
    HeartTransaction transaction =
        HeartTransaction.of(
            memberId, transactionType, content, amount, changedBalance, idempotencyKey);
    if (!heartTransactionRepository.saveIfAbsent(transaction)) {
      log.atInfo()
          .addKeyValue("transactionType", transactionType)
          .addKeyValue("idempotencyKey", idempotencyKey)
          .log("이미 처리된 하트 거래");
      return false;
    }
    balance.apply(transaction);
    return true;
  }
}
