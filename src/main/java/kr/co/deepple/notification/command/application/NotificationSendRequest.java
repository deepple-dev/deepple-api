package kr.co.deepple.notification.command.application;

import java.util.Map;
import kr.co.deepple.notification.command.domain.ChannelType;
import kr.co.deepple.notification.command.domain.NotificationType;
import kr.co.deepple.notification.command.domain.SenderType;
import lombok.NonNull;

public record NotificationSendRequest(
    @NonNull SenderType senderType,
    long senderId,
    long receiverId,
    @NonNull NotificationType notificationType,
    @NonNull Map<String, String> params,
    @NonNull ChannelType channelType) {}
