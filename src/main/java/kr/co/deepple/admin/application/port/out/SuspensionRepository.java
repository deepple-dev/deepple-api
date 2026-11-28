package kr.co.deepple.admin.application.port.out;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import kr.co.deepple.admin.domain.Suspension;
import kr.co.deepple.admin.domain.SuspensionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface SuspensionRepository extends JpaRepository<Suspension, Long> {
  Optional<Suspension> findByMemberId(long memberId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT s FROM Suspension s WHERE s.memberId = :memberId")
  Optional<Suspension> findByMemberIdForUpdate(long memberId);

  @Query(
      "SELECT s.memberId FROM Suspension s WHERE s.status = :status AND s.expireAt <= :now"
          + " ORDER BY s.expireAt")
  List<Long> findMemberIdsByStatusAndExpireAtLessThanEqual(SuspensionStatus status, Instant now);

  void deleteByMemberId(long memberId);
}
