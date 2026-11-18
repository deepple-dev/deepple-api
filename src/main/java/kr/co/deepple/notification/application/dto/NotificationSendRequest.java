package kr.co.deepple.notification.application.dto;

import java.util.Map;
import kr.co.deepple.notification.domain.ChannelType;
import kr.co.deepple.notification.domain.NotificationType;
import kr.co.deepple.notification.domain.SenderType;
import lombok.NonNull;

public record NotificationSendRequest(
    @NonNull String idempotencyKey,
    @NonNull SenderType senderType,
    long senderId,
    long receiverId,
    @NonNull NotificationType notificationType,
    @NonNull Map<String, String> params,
    @NonNull ChannelType channelType) {

  public static final String SENDER_NAME = "senderName";
}
