package deepple.deepple.notification.infra;

import deepple.deepple.notification.command.domain.ChannelType;
import deepple.deepple.notification.command.domain.DeviceRegistration;
import deepple.deepple.notification.command.domain.Notification;
import deepple.deepple.notification.command.domain.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "false")
public class FakeNotificationSender implements NotificationSender {

    @Override
    public ChannelType channel() {
        return ChannelType.PUSH;
    }

    @Override
    public void send(Notification notification, DeviceRegistration deviceRegistration) {
        log.info("[푸시 발송] receiverId={}, type={}, title={}, body={}",
            notification.getReceiverId(),
            notification.getType(),
            notification.getTitle(),
            notification.getBody()
        );
    }
}
