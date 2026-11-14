package kr.co.deepple.notification.domain;

public interface NotificationSender {
  ChannelType channel();

  void send(Notification notification, DeviceRegistration deviceRegistration) throws Exception;
}
