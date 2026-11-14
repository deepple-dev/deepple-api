package kr.co.deepple.notification.application;

import kr.co.deepple.notification.application.port.out.NotificationRepository;
import kr.co.deepple.notification.domain.Notification;
import kr.co.deepple.notification.domain.NotificationStatus;
import kr.co.deepple.notification.domain.NotificationType;
import kr.co.deepple.notification.domain.SenderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NotificationDataWriter {

  private final NotificationRepository notificationRepository;

  @Transactional
  public void save(Notification notification) {
    notificationRepository.save(notification);
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
    notificationRepository.save(failedNotification);
  }
}
