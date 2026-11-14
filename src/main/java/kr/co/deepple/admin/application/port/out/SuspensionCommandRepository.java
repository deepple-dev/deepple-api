package kr.co.deepple.admin.application.port.out;

import java.util.Optional;
import kr.co.deepple.admin.domain.Suspension;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuspensionCommandRepository extends JpaRepository<Suspension, Long> {
  Optional<Suspension> findByMemberId(long memberId);

  void deleteByMemberId(long memberId);
}
