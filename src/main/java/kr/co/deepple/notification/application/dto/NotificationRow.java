package kr.co.deepple.notification.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import kr.co.deepple.notification.domain.NotificationType;
import kr.co.deepple.notification.domain.SenderType;

public record NotificationRow(
    long notificationId,
    SenderType senderType,
    long senderId,
    long receiverId,
    NotificationType notificationType,
    String title,
    String body,
    boolean isRead,
    LocalDateTime createdAt) {
  @QueryProjection
  public NotificationRow {}
}
