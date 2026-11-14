package kr.co.deepple.notification.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.notification.domain.NotificationErrorCode;
import kr.co.deepple.notification.domain.NotificationType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationTypeMapper {

  public static NotificationType toNotificationType(String notificationType) {
    try {
      return NotificationType.valueOf(notificationType.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new DeeppleException(
          NotificationErrorCode.INVALID_NOTIFICATION_TYPE, "type=" + notificationType, e);
    }
  }
}
