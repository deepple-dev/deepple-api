package kr.co.deepple.notification.command.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceCommandRepository
    extends JpaRepository<NotificationPreference, Long> {
  boolean existsByMemberId(long memberId);

  Optional<NotificationPreference> findByMemberId(long memberId);
}
