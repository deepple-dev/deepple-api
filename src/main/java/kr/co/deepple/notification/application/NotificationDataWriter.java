package kr.co.deepple.notification.application;

import kr.co.deepple.notification.application.port.out.NotificationRepository;
import kr.co.deepple.notification.domain.Notification;
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
}
