package deepple.deepple.notification.command.application;

import deepple.deepple.notification.command.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NotificationDataWriter {

    private final NotificationCommandRepository notificationCommandRepository;

    @Transactional
    public void save(Notification notification) {
        notificationCommandRepository.save(notification);
    }

    @Transactional
    public void saveFailedNotification(
        SenderType senderType,
        Long senderId,
        Long receiverId,
        NotificationType type,
        String title,
        String body,
        NotificationStatus status
    ) {
        var failedNotification = Notification.createFailed(
            senderType,
            senderId,
            receiverId,
            type,
            title,
            body,
            status
        );
        notificationCommandRepository.save(failedNotification);
    }
}