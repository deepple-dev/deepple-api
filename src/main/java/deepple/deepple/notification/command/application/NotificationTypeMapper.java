package deepple.deepple.notification.command.application;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.notification.command.domain.NotificationErrorCode;
import deepple.deepple.notification.command.domain.NotificationType;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class NotificationTypeMapper {

    public static NotificationType toNotificationType(String notificationType) {
        try {
            return NotificationType.valueOf(notificationType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DeeppleException(NotificationErrorCode.INVALID_NOTIFICATION_TYPE, "type=" + notificationType, e);
        }
    }
}
