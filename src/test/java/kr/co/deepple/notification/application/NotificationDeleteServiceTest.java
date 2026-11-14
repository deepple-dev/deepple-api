package kr.co.deepple.notification.application;

import static org.mockito.Mockito.verify;

import java.util.List;
import kr.co.deepple.notification.application.dto.NotificationDeleteRequest;
import kr.co.deepple.notification.application.port.out.NotificationCommandRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationDeleteServiceTest {

  @Mock NotificationCommandRepository repository;

  @InjectMocks NotificationDeleteService service;

  @Test
  @DisplayName("delete(): 단일 알림 삭제 시 Repository 메서드 호출")
  void deleteSingleNotification() {
    // given
    long receiverId = 100L;
    long notificationId = 1L;
    var request = new NotificationDeleteRequest(List.of(notificationId));

    // when
    service.delete(request, receiverId);

    // then
    verify(repository).deleteAllByIdIn(List.of(notificationId), receiverId);
  }

  @Test
  @DisplayName("delete(): 여러 알림 일괄 삭제 시 Repository 메서드 호출")
  void deleteMultipleNotifications() {
    // given
    long receiverId = 100L;
    var ids = List.of(1L, 2L, 3L);
    var request = new NotificationDeleteRequest(ids);

    // when
    service.delete(request, receiverId);

    // then
    verify(repository).deleteAllByIdIn(ids, receiverId);
  }
}
