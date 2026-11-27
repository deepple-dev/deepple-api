package kr.co.deepple.notification.application.port.out;

import java.util.List;
import kr.co.deepple.notification.application.dto.NotificationRow;

public interface NotificationQueryRepository {
  List<NotificationRow> findNotifications(long receiverId, Long lastId, int limit);
}
