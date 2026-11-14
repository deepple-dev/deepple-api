package kr.co.deepple.notification.adapter.out.fcm;

import kr.co.deepple.notification.domain.ChannelType;
import kr.co.deepple.notification.domain.DeviceRegistration;
import kr.co.deepple.notification.domain.Notification;
import kr.co.deepple.notification.domain.NotificationSender;
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
    log.info(
        "[푸시 발송] receiverId={}, type={}, title={}, body={}",
        notification.getReceiverId(),
        notification.getType(),
        notification.getTitle(),
        notification.getBody());
  }
}
