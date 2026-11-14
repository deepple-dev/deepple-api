package kr.co.deepple.admin.application.port.out;

import kr.co.deepple.admin.domain.Screening;
import kr.co.deepple.admin.domain.ScreeningStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {
  boolean existsByMemberId(long memberId);

  boolean existsByMemberIdAndStatus(long memberId, ScreeningStatus status);
}
