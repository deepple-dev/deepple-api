package kr.co.deepple.notification.command.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTemplateCommandRepository extends JpaRepository<NotificationTemplate, Long> {
    Optional<NotificationTemplate> findByType(NotificationType type);
}
