package kr.co.deepple.admin.application.port.out;

import kr.co.deepple.admin.domain.Warning;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarningCommandRepository extends JpaRepository<Warning, Long> {
  long countByMemberIdAndIsCriticalTrue(long memberId);

  boolean existsByIdempotencyKey(String idempotencyKey);
}
