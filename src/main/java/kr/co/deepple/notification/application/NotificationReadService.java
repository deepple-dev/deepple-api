package kr.co.deepple.notification.application;

import kr.co.deepple.notification.application.dto.NotificationReadRequest;
import kr.co.deepple.notification.application.port.out.NotificationCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationReadService {

  private final NotificationCommandRepository notificationCommandRepository;

  @Transactional
  public void markAsRead(NotificationReadRequest request, long receiverId) {
    notificationCommandRepository.markAllAsReadByIdIn(request.notificationIds(), receiverId);
  }
}
