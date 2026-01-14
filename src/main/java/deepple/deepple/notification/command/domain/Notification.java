package deepple.deepple.notification.command.domain;

import deepple.deepple.common.entity.SoftDeleteBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(
    name = "notifications",
    indexes = @Index(name = "idx_notifications_receiver_deleted_id", columnList = "receiverId, deletedAt, id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Notification extends SoftDeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(STRING)
    @Column(columnDefinition = "varchar(50)")
    private SenderType senderType;

    private Long senderId;

    private Long receiverId;

    @Enumerated(STRING)
    @Column(columnDefinition = "varchar(50)")
    private NotificationType type;

    private String title;

    private String body;

    private LocalDateTime readAt = null;

    @Enumerated(STRING)
    @Column(columnDefinition = "varchar(50)")
    private NotificationStatus status;

    private Notification(
        SenderType senderType,
        Long senderId,
        Long receiverId,
        NotificationType type,
        String title,
        String body
    ) {
        this.senderType = senderType;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.type = type;
        this.title = title;
        this.body = body;
        status = NotificationStatus.CREATED;
    }

    private Notification(
        SenderType senderType,
        Long senderId,
        Long receiverId,
        NotificationType type,
        String title,
        String body,
        NotificationStatus status
    ) {
        this.senderType = senderType;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.type = type;
        this.title = title;
        this.body = body;
        this.status = status;
    }

    public static Notification create(
        @NonNull SenderType senderType,
        long senderId,
        long receiverId,
        @NonNull NotificationType type,
        String title,
        String body
    ) {
        return new Notification(senderType, senderId, receiverId, type, title, body);
    }

    public static Notification createFailed(
        @NonNull SenderType senderType,
        long senderId,
        long receiverId,
        @NonNull NotificationType type,
        String title,
        String body,
        @NonNull NotificationStatus status
    ) {
        return new Notification(senderType, senderId, receiverId, type, title, body, status);
    }

    public boolean isRead() {
        return readAt != null;
    }

    public void markAsRead() {
        if (this.readAt != null) {
            return;
        }
        readAt = LocalDateTime.now();
    }

    public void markAsSent() {
        status = NotificationStatus.SENT;
    }
}
