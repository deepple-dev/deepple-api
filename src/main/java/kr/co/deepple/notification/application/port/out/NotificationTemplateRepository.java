package kr.co.deepple.notification.application.port.out;

import java.util.Optional;
import kr.co.deepple.notification.domain.NotificationTemplate;
import kr.co.deepple.notification.domain.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
  Optional<NotificationTemplate> findByType(NotificationType type);
}
