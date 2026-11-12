package kr.co.deepple.admin.command.domain.screening;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScreeningCommandRepository extends JpaRepository<Screening, Long> {

    boolean existsByMemberId(long memberId);

    boolean existsByMemberIdAndStatus(long memberId, ScreeningStatus status);
}
