package kr.co.deepple.notification.application.port.out;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import kr.co.deepple.notification.domain.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface NotificationPreferenceRepository
    extends JpaRepository<NotificationPreference, Long> {
  boolean existsByMemberId(long memberId);

  Optional<NotificationPreference> findByMemberId(long memberId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM NotificationPreference p WHERE p.memberId = :memberId")
  Optional<NotificationPreference> findByMemberIdForUpdate(long memberId);
}
