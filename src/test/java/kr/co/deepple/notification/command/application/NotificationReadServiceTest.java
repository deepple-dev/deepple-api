package kr.co.deepple.notification.command.application;

import static org.mockito.Mockito.verify;

import java.util.List;
import kr.co.deepple.notification.command.domain.NotificationCommandRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationReadServiceTest {

  @Mock NotificationCommandRepository repository;

  @InjectMocks NotificationReadService service;

  @Test
  @DisplayName("markAsRead(): 단일 알림 읽음 처리 시 Repository 메서드 호출")
  void markAsReadSingleNotification() {
    // given
    long receiverId = 100L;
    long notificationId = 1L;
    var request = new NotificationReadRequest(List.of(notificationId));

    // when
    service.markAsRead(request, receiverId);

    // then
    verify(repository).markAllAsReadByIdIn(List.of(notificationId), receiverId);
  }

  @Test
  @DisplayName("markAsRead(): 여러 알림 일괄 읽음 처리 시 Repository 메서드 호출")
  void markAsReadMultipleNotifications() {
    // given
    long receiverId = 100L;
    var ids = List.of(1L, 2L, 3L);
    var request = new NotificationReadRequest(ids);

    // when
    service.markAsRead(request, receiverId);

    // then
    verify(repository).markAllAsReadByIdIn(ids, receiverId);
  }
}
