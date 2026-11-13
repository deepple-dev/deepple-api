package kr.co.deepple.admin.command.domain.suspension;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuspensionCommandRepository extends JpaRepository<Suspension, Long> {
  Optional<Suspension> findByMemberId(long memberId);

  void deleteByMemberId(long memberId);
}
