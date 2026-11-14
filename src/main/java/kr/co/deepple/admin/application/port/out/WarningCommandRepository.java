package kr.co.deepple.admin.application.port.out;

import java.util.Collection;
import java.util.List;
import kr.co.deepple.admin.domain.Warning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WarningCommandRepository extends JpaRepository<Warning, Long> {
  long countByMemberIdAndIsCriticalTrue(long memberId);

  @Query(
      """
      SELECT w.memberId AS memberId, COUNT(w) AS warningCount
      FROM Warning w
      WHERE w.memberId IN :memberIds
      GROUP BY w.memberId
      """)
  List<MemberWarningCount> countByMemberIds(Collection<Long> memberIds);

  boolean existsByIdempotencyKey(String idempotencyKey);

  interface MemberWarningCount {
    long getMemberId();

    long getWarningCount();
  }
}
