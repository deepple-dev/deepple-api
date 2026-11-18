package kr.co.deepple.notification.application;

import static kr.co.deepple.notification.domain.NotificationStatus.FAILED_DEVICE_NOT_FOUND;
import static kr.co.deepple.notification.domain.NotificationStatus.FAILED_EXCEPTION;
import static kr.co.deepple.notification.domain.NotificationStatus.FAILED_PREFERENCE_NOT_FOUND;
import static kr.co.deepple.notification.domain.NotificationStatus.FAILED_TEMPLATE_NOT_FOUND;
import static kr.co.deepple.notification.domain.NotificationStatus.FAILED_UNSUPPORTED_CHANNEL;
import static kr.co.deepple.notification.domain.NotificationStatus.REJECTED_BY_PREFERENCE;

import kr.co.deepple.notification.application.dto.NotificationSendRequest;
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

  public void send(NotificationSendRequest request) {
    if (reader.existsNotification(request.idempotencyKey())) {
      return;
    }

    Notification notification = createNotificationWithTemplate(request);
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

    sendNotification(notification, device, request);
  }

  private Notification createNotificationWithTemplate(NotificationSendRequest request) {
    return reader
        .findTemplate(request.notificationType())
        .map(template ->
            Notification.of(
                request.idempotencyKey(),
                request.senderType(),
                request.senderId(),
                request.receiverId(),
                request.notificationType(),
                template.generateTitle(request.params()),
                template.generateBody(request.params())))
        .orElseGet(() -> {
          log.atError()
              .addKeyValue("notificationType", request.notificationType())
              .addKeyValue("receiverId", request.receiverId())
              .log("알림 템플릿 없음");
          writer.save(
              Notification.createFailed(
                  request.idempotencyKey(),
                  request.senderType(),
                  request.senderId(),
                  request.receiverId(),
                  request.notificationType(),
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
      Notification notification, DeviceRegistration device, NotificationSendRequest request) {
    notificationSenderResolver
        .resolve(request.channelType())
        .ifPresentOrElse(
            sender -> dispatch(sender, notification, device),
            () -> handleUnsupportedChannel(notification, request));
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
      Notification notification, NotificationSendRequest request) {
    log.atWarn()
        .addKeyValue("channel", request.channelType())
        .addKeyValue("receiverId", request.receiverId())
        .log("지원하지 않는 알림 채널");
    saveFailed(notification, FAILED_UNSUPPORTED_CHANNEL);
  }

  private void saveFailed(Notification notification, NotificationStatus status) {
    notification.markAsFailed(status);
    writer.save(notification);
  }
}
