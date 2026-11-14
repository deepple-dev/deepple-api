package kr.co.deepple.notification.application;

import kr.co.deepple.notification.application.dto.NotificationDeleteRequest;
import kr.co.deepple.notification.application.port.out.NotificationCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationDeleteService {

  private final NotificationCommandRepository notificationCommandRepository;

  @Transactional
  public void delete(NotificationDeleteRequest request, long receiverId) {
    notificationCommandRepository.deleteAllByIdIn(request.notificationIds(), receiverId);
  }
}
