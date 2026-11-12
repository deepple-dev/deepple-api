package kr.co.deepple.notification.query;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.co.deepple.notification.command.domain.NotificationType;

public record NotificationView(
    long notificationId,
    long senderId,
    long receiverId,
    @Schema(implementation = NotificationType.class) String notificationType,
    String title,
    String body,
    boolean isRead,
    LocalDateTime createdAt) {
  @QueryProjection
  public NotificationView {}
}
