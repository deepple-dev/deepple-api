package deepple.deepple.notification.infra;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import deepple.deepple.notification.command.application.NotificationSendFailedException;
import deepple.deepple.notification.command.domain.ChannelType;
import deepple.deepple.notification.command.domain.DeviceRegistration;
import deepple.deepple.notification.command.domain.Notification;
import deepple.deepple.notification.command.domain.NotificationSender;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static deepple.deepple.notification.command.infra.FcmResilienceConfig.CIRCUIT_BREAKER_POLICY_NAME;
import static deepple.deepple.notification.command.infra.FcmResilienceConfig.RETRY_POLICY_NAME;

@Slf4j
@Service
public class FcmNotificationSender implements NotificationSender {

    private static final String KEY_SENDER_ID = "senderId";
    private static final String KEY_SENDER_TYPE = "senderType";
    private static final String KEY_RECEIVER_ID = "receiverId";
    private static final String KEY_NOTIFICATION_TYPE = "notificationType";

    @Override
    public ChannelType channel() {
        return ChannelType.PUSH;
    }

    @Override
    @Retry(name = RETRY_POLICY_NAME, fallbackMethod = "sendFallback")
    @CircuitBreaker(name = CIRCUIT_BREAKER_POLICY_NAME)
    public void send(Notification notification, DeviceRegistration deviceRegistration) throws Exception {
        var message = Message.builder()
            .setToken(deviceRegistration.getRegistrationToken())
            .setNotification(
                com.google.firebase.messaging.Notification.builder()
                    .setTitle(notification.getTitle())
                    .setBody(notification.getBody())
                    .build()
            )
            .putData(KEY_SENDER_ID, String.valueOf(notification.getSenderId()))
            .putData(KEY_SENDER_TYPE, notification.getSenderType().toString())
            .putData(KEY_RECEIVER_ID, String.valueOf(notification.getReceiverId()))
            .putData(KEY_NOTIFICATION_TYPE, notification.getType().toString())
            .build();

        String messageId = FirebaseMessaging.getInstance().send(message);
        log.info("[알림 전송 성공] messageId={}, receiverId={}", messageId, notification.getReceiverId());
    }

    @SuppressWarnings("unused")
    private void sendFallback(Notification notification, DeviceRegistration deviceRegistration, Exception exception) {
        if (exception instanceof FirebaseMessagingException fme) {
            log.warn("[FCM 전송 실패] receiverId={}, errorCode={}, message={}",
                notification.getReceiverId(),
                fme.getMessagingErrorCode(),
                fme.getMessage()
            );
        } else {
            log.warn("[FCM 전송 실패] receiverId={}, exceptionType={}, message={}",
                notification.getReceiverId(),
                exception.getClass().getSimpleName(),
                exception.getMessage()
            );
        }
        throw new NotificationSendFailedException(exception);
    }
}
