package kr.co.deepple.notification.application;

import java.util.List;
import kr.co.deepple.notification.application.dto.NotificationView;
import kr.co.deepple.notification.application.dto.NotificationViews;
import kr.co.deepple.notification.application.port.out.NotificationQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

  private static final int CLIENT_PAGE_SIZE = 20;
  private final NotificationQueryRepository notificationQueryRepository;

  public NotificationViews findNotifications(long receiverId, Long lastId) {
    List<NotificationView> views =
        notificationQueryRepository.findNotifications(receiverId, lastId, CLIENT_PAGE_SIZE + 1);
    List<NotificationView> notifications = views.stream().limit(CLIENT_PAGE_SIZE).toList();
    boolean hasMore = views.size() > CLIENT_PAGE_SIZE;

    return new NotificationViews(notifications, hasMore);
  }
}
