package kr.co.deepple.notification.application.port.out;

import java.util.List;
import kr.co.deepple.notification.application.dto.NotificationView;

public interface NotificationQueryRepository {
  List<NotificationView> findNotifications(long receiverId, Long lastId, int limit);
}
