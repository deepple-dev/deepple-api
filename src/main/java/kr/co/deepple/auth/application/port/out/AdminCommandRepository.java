package kr.co.deepple.auth.application.port.out;

import java.util.Optional;
import kr.co.deepple.auth.domain.Admin;
import kr.co.deepple.auth.domain.ApprovalStatus;
import kr.co.deepple.auth.domain.Email;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminCommandRepository extends JpaRepository<Admin, Long> {
  Optional<Admin> findByEmail(Email email);

  Optional<Admin> findByIdAndApprovalStatus(long id, ApprovalStatus approvalStatus);
}
