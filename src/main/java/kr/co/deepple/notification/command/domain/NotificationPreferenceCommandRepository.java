package kr.co.deepple.notification.command.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationPreferenceCommandRepository extends JpaRepository<NotificationPreference, Long> {
    boolean existsByMemberId(long memberId);

    Optional<NotificationPreference> findByMemberId(long memberId);
}
