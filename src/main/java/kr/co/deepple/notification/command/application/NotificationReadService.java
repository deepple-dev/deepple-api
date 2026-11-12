package kr.co.deepple.notification.command.application;

import kr.co.deepple.notification.command.domain.NotificationCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationReadService {

    private final NotificationCommandRepository notificationCommandRepository;

    @Transactional
    public void markAsRead(NotificationReadRequest request, long receiverId) {
        notificationCommandRepository.markAllAsReadByIdIn(request.notificationIds(), receiverId);
    }
}