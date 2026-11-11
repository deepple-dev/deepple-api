package kr.co.deepple.notification.command.application;

import kr.co.deepple.notification.command.domain.NotificationCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationDeleteService {

    private final NotificationCommandRepository notificationCommandRepository;

    @Transactional
    public void delete(NotificationDeleteRequest request, long receiverId) {
        notificationCommandRepository.deleteAllByIdIn(request.notificationIds(), receiverId);
    }
}
