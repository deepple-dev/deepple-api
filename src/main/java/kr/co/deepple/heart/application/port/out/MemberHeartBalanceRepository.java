package kr.co.deepple.heart.application.port.out;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import kr.co.deepple.heart.domain.MemberHeartBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MemberHeartBalanceRepository extends JpaRepository<MemberHeartBalance, Long> {
  Optional<MemberHeartBalance> findByMemberId(long memberId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT b FROM MemberHeartBalance b WHERE b.memberId = :memberId")
  Optional<MemberHeartBalance> findByMemberIdForUpdate(long memberId);

  @Modifying
  @Query(
      value =
          """
          insert into heart_balances (member_id, mission_balance, purchase_balance, created_at)
          values (:memberId, 0, 0, :createdAt)
          on duplicate key update id = id
          """,
      nativeQuery = true)
  void insertIfAbsent(long memberId, LocalDateTime createdAt);
}
