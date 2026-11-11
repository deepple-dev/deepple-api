package kr.co.deepple.admin.command.domain.suspension;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuspensionCommandRepository extends JpaRepository<Suspension, Long> {
    Optional<Suspension> findByMemberId(long memberId);

    Optional<Suspension> findByMemberIdAndStatusOrderByExpireAtDesc(long memberId, SuspensionStatus status);

    void deleteByMemberId(long memberId);
}
