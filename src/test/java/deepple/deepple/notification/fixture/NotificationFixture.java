package deepple.deepple.notification.fixture;

import deepple.deepple.notification.command.domain.Notification;
import deepple.deepple.notification.command.domain.NotificationStatus;
import deepple.deepple.notification.command.domain.NotificationType;
import deepple.deepple.notification.command.domain.SenderType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationFixture {

    private Long id;
    private SenderType senderType = SenderType.MEMBER;
    private long senderId = 1L;
    private long receiverId = 2L;
    private NotificationType type = NotificationType.MATCH_REQUEST;
    private String title = "알림 제목";
    private String body = "알림 내용";
    private NotificationStatus status = NotificationStatus.CREATED;
    private LocalDateTime readAt;

    public static NotificationFixture builder() {
        return new NotificationFixture();
    }

    public Notification build() {
        Notification notification = Notification.create(senderType, senderId, receiverId, type, title, body);
        setField(notification, "id", id);
        setField(notification, "status", status);
        setField(notification, "readAt", readAt);
        return notification;
    }
}
