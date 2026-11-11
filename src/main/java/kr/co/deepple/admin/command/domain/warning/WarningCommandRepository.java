package kr.co.deepple.admin.command.domain.warning;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WarningCommandRepository extends JpaRepository<Warning, Long> {
    long countByMemberIdAndIsCriticalTrue(long memberId);
}
