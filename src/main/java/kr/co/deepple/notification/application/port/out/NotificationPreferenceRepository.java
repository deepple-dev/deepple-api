package kr.co.deepple.notification.application.port.out;

import java.util.Optional;
import kr.co.deepple.notification.domain.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceRepository
    extends JpaRepository<NotificationPreference, Long> {
  boolean existsByMemberId(long memberId);

  Optional<NotificationPreference> findByMemberId(long memberId);
}
