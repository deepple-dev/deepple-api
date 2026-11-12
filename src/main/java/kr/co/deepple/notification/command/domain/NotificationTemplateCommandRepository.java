package kr.co.deepple.notification.command.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationTemplateCommandRepository
    extends JpaRepository<NotificationTemplate, Long> {
  Optional<NotificationTemplate> findByType(NotificationType type);
}
