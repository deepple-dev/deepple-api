package kr.co.deepple.admin.command.domain.admin;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminCommandRepository extends JpaRepository<Admin, Long> {
  Optional<Admin> findByEmail(Email email);

  Optional<Admin> findByIdAndApprovalStatus(long id, ApprovalStatus approvalStatus);
}
