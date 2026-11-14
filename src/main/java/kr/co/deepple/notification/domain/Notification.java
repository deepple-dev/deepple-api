package kr.co.deepple.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import kr.co.deepple.common.entity.SoftDeleteBaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends SoftDeleteBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private SenderType senderType;

  private long senderId;

  private long receiverId;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private NotificationType type;

  private String title;

  private String body;

  private LocalDateTime readAt;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private NotificationStatus status;

  private Notification(
      SenderType senderType,
      long senderId,
      long receiverId,
      NotificationType type,
      String title,
      String body,
      NotificationStatus status) {
    this.senderType = senderType;
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.type = type;
    this.title = title;
    this.body = body;
    this.status = status;
  }

  public static Notification of(
      @NonNull SenderType senderType,
      long senderId,
      long receiverId,
      @NonNull NotificationType type,
      String title,
      String body) {
    return new Notification(
        senderType, senderId, receiverId, type, title, body, NotificationStatus.CREATED);
  }

  public static Notification createFailed(
      @NonNull SenderType senderType,
      long senderId,
      long receiverId,
      @NonNull NotificationType type,
      String title,
      String body,
      @NonNull NotificationStatus status) {
    return new Notification(senderType, senderId, receiverId, type, title, body, status);
  }

  public boolean isRead() {
    return readAt != null;
  }

  public void markAsRead() {
    if (readAt != null) {
      return;
    }
    readAt = LocalDateTime.now();
  }

  public void markAsSent() {
    status = NotificationStatus.SENT;
  }
}
