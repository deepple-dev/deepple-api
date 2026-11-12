package kr.co.deepple.notification.command.application;

import kr.co.deepple.notification.command.domain.Notification;
import kr.co.deepple.notification.command.domain.NotificationCommandRepository;
import kr.co.deepple.notification.command.domain.NotificationStatus;
import kr.co.deepple.notification.command.domain.NotificationType;
import kr.co.deepple.notification.command.domain.SenderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NotificationDataWriter {

  private final NotificationCommandRepository notificationCommandRepository;

  @Transactional
  public void save(Notification notification) {
    notificationCommandRepository.save(notification);
  }

  @Transactional
  public void saveFailedNotification(
      SenderType senderType,
      long senderId,
      long receiverId,
      NotificationType type,
      String title,
      String body,
      NotificationStatus status) {
    Notification failedNotification =
        Notification.createFailed(senderType, senderId, receiverId, type, title, body, status);
    notificationCommandRepository.save(failedNotification);
  }
}
