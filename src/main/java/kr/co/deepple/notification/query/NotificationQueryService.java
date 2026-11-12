package kr.co.deepple.notification.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private static final int CLIENT_PAGE_SIZE = 20;
    private final NotificationQueryRepository notificationQueryRepository;

    public NotificationViews findNotifications(long receiverId, Long lastId) {
        List<NotificationView> views = notificationQueryRepository.findNotifications(
            receiverId,
            lastId,
            CLIENT_PAGE_SIZE + 1
        );
        List<NotificationView> notifications = views.stream()
            .limit(CLIENT_PAGE_SIZE)
            .toList();
        boolean hasMore = views.size() > CLIENT_PAGE_SIZE;

        return new NotificationViews(notifications, hasMore);
    }
}