package kr.co.deepple.notification.application;

import kr.co.deepple.notification.application.dto.NotificationDeleteRequest;
import kr.co.deepple.notification.application.port.out.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationDeleteService {

  private final NotificationRepository notificationRepository;

  @Transactional
  public void delete(NotificationDeleteRequest request, long receiverId) {
    notificationRepository.deleteAllByIdIn(request.notificationIds(), receiverId);
  }
}
