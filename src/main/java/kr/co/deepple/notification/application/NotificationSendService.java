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
          saveFailedNotification(request, FAILED_TEMPLATE_NOT_FOUND);
          return null;
        });
  }

  private boolean canSendByPreference(Notification notification) {
    return reader
        .findPreference(notification.getReceiverId())
        .map(pref -> {
          boolean canSend = pref.canReceive(notification.getType());
          if (!canSend) {
            saveFailedNotification(notification, REJECTED_BY_PREFERENCE);
          }
          return canSend;
        })
        .orElseGet(() -> {
          log.atWarn().addKeyValue("receiverId", notification.getReceiverId()).log("알림 설정 없음");
          saveFailedNotification(notification, FAILED_PREFERENCE_NOT_FOUND);
          return false;
        });
  }

  private DeviceRegistration findReceiversActiveDevice(Notification notification) {
    return reader
        .findActiveDevice(notification.getReceiverId())
        .orElseGet(() -> {
          saveFailedNotification(notification, FAILED_DEVICE_NOT_FOUND);
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
    try {
      sender.send(notification, deviceRegistration);
      notification.markAsSent();
      writer.save(notification);
    } catch (Exception e) {
      log.atWarn()
          .setCause(e)
          .addKeyValue("receiverId", notification.getReceiverId())
          .addKeyValue("notificationType", notification.getType())
          .log("알림 전송 실패");
      saveFailedNotification(notification, FAILED_EXCEPTION);
    }
  }

  private void handleUnsupportedChannel(
      Notification notification, NotificationSendRequest request) {
    log.atWarn()
        .addKeyValue("channel", request.channelType())
        .addKeyValue("receiverId", request.receiverId())
        .log("지원하지 않는 알림 채널");
    saveFailedNotification(notification, FAILED_UNSUPPORTED_CHANNEL);
  }

  private void saveFailedNotification(NotificationSendRequest request, NotificationStatus status) {
    writer.saveFailedNotification(
        request.senderType(),
        request.senderId(),
        request.receiverId(),
        request.notificationType(),
        "알림 전송 실패",
        "알림 전송에 실패했습니다.",
        status);
  }

  private void saveFailedNotification(Notification notification, NotificationStatus status) {
    writer.saveFailedNotification(
        notification.getSenderType(),
        notification.getSenderId(),
        notification.getReceiverId(),
        notification.getType(),
        notification.getTitle(),
        notification.getBody(),
        status);
  }
}
