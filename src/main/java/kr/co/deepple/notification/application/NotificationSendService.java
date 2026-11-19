package kr.co.deepple.notification.application;

import static kr.co.deepple.notification.domain.NotificationStatus.FAILED_DEVICE_NOT_FOUND;
import static kr.co.deepple.notification.domain.NotificationStatus.FAILED_EXCEPTION;
import static kr.co.deepple.notification.domain.NotificationStatus.FAILED_PREFERENCE_NOT_FOUND;
import static kr.co.deepple.notification.domain.NotificationStatus.FAILED_TEMPLATE_NOT_FOUND;
import static kr.co.deepple.notification.domain.NotificationStatus.FAILED_UNSUPPORTED_CHANNEL;
import static kr.co.deepple.notification.domain.NotificationStatus.REJECTED_BY_PREFERENCE;

import kr.co.deepple.notification.application.port.in.SendNotificationCommand;
import kr.co.deepple.notification.domain.DeviceRegistration;
import kr.co.deepple.notification.domain.Notification;
import kr.co.deepple.notification.domain.NotificationSender;
import kr.co.deepple.notification.domain.NotificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSendService {

  private final NotificationDataReader reader;
  private final NotificationDataWriter writer;
  private final NotificationSenderResolver notificationSenderResolver;

  public void send(SendNotificationCommand command) {
    if (reader.existsNotification(command.idempotencyKey())) {
      return;
    }

    Notification notification = createNotificationWithTemplate(command);
    if (notification == null) {
      return;
    }

    if (!canSendByPreference(notification)) {
      return;
    }

    DeviceRegistration device = findReceiversActiveDevice(notification);
    if (device == null) {
      return;
    }

    sendNotification(notification, device, command);
  }

  private Notification createNotificationWithTemplate(SendNotificationCommand command) {
    return reader
        .findTemplate(command.notificationType())
        .map(template ->
            Notification.of(
                command.idempotencyKey(),
                command.senderType(),
                command.senderId(),
                command.receiverId(),
                command.notificationType(),
                template.generateTitle(command.params()),
                template.generateBody(command.params())))
        .orElseGet(() -> {
          log.atError()
              .addKeyValue("notificationType", command.notificationType())
              .addKeyValue("receiverId", command.receiverId())
              .log("알림 템플릿 없음");
          writer.save(
              Notification.createFailed(
                  command.idempotencyKey(),
                  command.senderType(),
                  command.senderId(),
                  command.receiverId(),
                  command.notificationType(),
                  "알림 전송 실패",
                  "알림 전송에 실패했습니다.",
                  FAILED_TEMPLATE_NOT_FOUND));
          return null;
        });
  }

  private boolean canSendByPreference(Notification notification) {
    return reader
        .findPreference(notification.getReceiverId())
        .map(pref -> {
          boolean canSend = pref.canReceive(notification.getType());
          if (!canSend) {
            saveFailed(notification, REJECTED_BY_PREFERENCE);
          }
          return canSend;
        })
        .orElseGet(() -> {
          log.atWarn().addKeyValue("receiverId", notification.getReceiverId()).log("알림 설정 없음");
          saveFailed(notification, FAILED_PREFERENCE_NOT_FOUND);
          return false;
        });
  }

  private DeviceRegistration findReceiversActiveDevice(Notification notification) {
    return reader
        .findActiveDevice(notification.getReceiverId())
        .orElseGet(() -> {
          saveFailed(notification, FAILED_DEVICE_NOT_FOUND);
          return null;
        });
  }

  private void sendNotification(
      Notification notification, DeviceRegistration device, SendNotificationCommand command) {
    notificationSenderResolver
        .resolve(command.channelType())
        .ifPresentOrElse(
            sender -> dispatch(sender, notification, device),
            () -> handleUnsupportedChannel(notification, command));
  }

  private void dispatch(
      NotificationSender sender, Notification notification, DeviceRegistration deviceRegistration) {
    writer.save(notification);
    try {
      sender.send(notification, deviceRegistration);
      notification.markAsSent();
    } catch (Exception e) {
      log.atWarn()
          .setCause(e)
          .addKeyValue("receiverId", notification.getReceiverId())
          .addKeyValue("notificationType", notification.getType())
          .log("알림 전송 실패");
      notification.markAsFailed(FAILED_EXCEPTION);
    }
    writer.save(notification);
  }

  private void handleUnsupportedChannel(
      Notification notification, SendNotificationCommand command) {
    log.atWarn()
        .addKeyValue("channel", command.channelType())
        .addKeyValue("receiverId", command.receiverId())
        .log("지원하지 않는 알림 채널");
    saveFailed(notification, FAILED_UNSUPPORTED_CHANNEL);
  }

  private void saveFailed(Notification notification, NotificationStatus status) {
    notification.markAsFailed(status);
    writer.save(notification);
  }
}
