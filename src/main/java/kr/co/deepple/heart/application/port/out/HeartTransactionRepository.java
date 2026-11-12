package kr.co.deepple.heart.application.port.out;

import java.time.LocalDateTime;
import kr.co.deepple.heart.domain.HeartTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HeartTransactionRepository extends JpaRepository<HeartTransaction, Long> {
  default boolean saveIfAbsent(HeartTransaction transaction) {
    if (transaction.getIdempotencyKey() == null) {
      save(transaction);
      return true;
    }
    return insertIfAbsent(transaction, LocalDateTime.now()) > 0;
  }

  @Modifying
  @Query(
      value =
          """
          insert ignore into heart_transactions
              (member_id, transaction_type, content, amount, mission_balance, purchase_balance,
               idempotency_key, created_at)
          values
              (:#{#transaction.memberId}, :#{#transaction.transactionType.name()}, :#{#transaction.content},
               :#{#transaction.amount}, :#{#transaction.balance.missionBalance},
               :#{#transaction.balance.purchaseBalance}, :#{#transaction.idempotencyKey}, :createdAt)
          """,
      nativeQuery = true)
  int insertIfAbsent(
      @Param("transaction") HeartTransaction transaction,
      @Param("createdAt") LocalDateTime createdAt);
}
