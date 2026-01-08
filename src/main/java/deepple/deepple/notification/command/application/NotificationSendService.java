package deepple.deepple.notification.command.application;

import deepple.deepple.notification.command.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static deepple.deepple.notification.command.domain.NotificationStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSendService {

    private final NotificationCommandRepository notificationCommandRepository;
    private final NotificationPreferenceCommandRepository notificationPreferenceRepository;
    private final NotificationTemplateCommandRepository notificationTemplateRepository;
    private final DeviceRegistrationCommandRepository deviceRegistrationCommandRepository;
    private final NotificationSenderResolver notificationSenderResolver;

    @Transactional
    public void send(NotificationSendRequest request) {
        // 1. 알림 생성
        var notification = createNotificationWithTemplate(request);
        if (notification == null) {
            return;
        }

        // 2. 수신자 설정 확인
        if (!canSendByPreference(notification)) {
            return;
        }

        // 3. 수신자 기기 조회
        var device = findReceiversActiveDevice(notification);
        if (device == null) {
            return;
        }

        // 4. 전송
        sendNotification(notification, device, request);
    }

    // TODO: 삭제 필요(k6 부하 테스트 용도 - FCM 전송 제외)
    @Transactional
    public void sendWithoutPush(NotificationSendRequest request) {
        // 1. 알림 생성
        var notification = createNotificationWithTemplate(request);
        if (notification == null) {
            return;
        }

        // 2. 수신자 설정 확인
        if (!canSendByPreference(notification)) {
            return;
        }

        // 3. 수신자 기기 조회
        var device = findReceiversActiveDevice(notification);
        if (device == null) {
            return;
        }

        // 4. FCM 전송 시간 시뮬레이션 (50ms)
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 5. 저장
        notification.markAsSent();
        save(notification);
    }

    private Notification createNotificationWithTemplate(NotificationSendRequest request) {
        return notificationTemplateRepository.findByType(request.notificationType())
            .map(template -> Notification.create(
                request.senderType(),
                request.senderId(),
                request.receiverId(),
                request.notificationType(),
                template.generateTitle(request.params()),
                template.generateBody(request.params())
            ))
            .orElseGet(() -> {
                log.error("[알림 템플릿 조회 실패] type={}, receiverId={}", request.notificationType(), request.receiverId());
                saveFailedNotification(request, FAILED_TEMPLATE_NOT_FOUND);
                return null;
            });
    }

    private boolean canSendByPreference(Notification notification) {
        return notificationPreferenceRepository.findByMemberId(notification.getReceiverId())
            .map(pref -> {
                boolean canSend = pref.canReceive(notification.getType());
                if (!canSend) {
                    saveFailedNotification(notification, REJECTED_BY_PREFERENCE);
                }
                return canSend;
            })
            .orElseGet(() -> {
                log.warn("[알림 설정 조회 실패] receiverId={}", notification.getReceiverId());
                saveFailedNotification(notification, FAILED_PREFERENCE_NOT_FOUND);
                return false;
            });
    }

    private DeviceRegistration findReceiversActiveDevice(Notification notification) {
        return deviceRegistrationCommandRepository.findByMemberIdAndIsActiveTrue(notification.getReceiverId())
            .orElseGet(() -> {
                log.warn("[디바이스 정보 조회 실패] receiverId={}", notification.getReceiverId());
                saveFailedNotification(notification, FAILED_DEVICE_NOT_FOUND);
                return null;
            });
    }

    private void sendNotification(
        Notification notification,
        DeviceRegistration device,
        NotificationSendRequest request
    ) {
        notificationSenderResolver.resolve(request.channelType())
            .ifPresentOrElse(
                sender -> dispatch(sender, notification, device),
                () -> handleUnsupportedChannel(notification, request)
            );
    }

    private void dispatch(NotificationSender sender, Notification notification, DeviceRegistration deviceRegistration) {
        try {
            sender.send(notification, deviceRegistration);
            notification.markAsSent();
            save(notification);
        } catch (Exception e) {
            log.warn("[알림 전송 실패] receiverId={}, type={}", notification.getReceiverId(), notification.getType(), e);
            saveFailedNotification(notification, FAILED_EXCEPTION);
        }
    }

    private void handleUnsupportedChannel(Notification notification, NotificationSendRequest request) {
        log.warn("[지원하지 않는 채널] channel={}, receiverId={}", request.channelType(), request.receiverId());
        saveFailedNotification(notification, FAILED_UNSUPPORTED_CHANNEL);
    }

    private void saveFailedNotification(NotificationSendRequest request, NotificationStatus status) {
        var failedNotification = Notification.createFailed(
            request.senderType(),
            request.senderId(),
            request.receiverId(),
            request.notificationType(),
            "알림 전송 실패",
            "알림 전송에 실패했습니다.",
            status
        );
        save(failedNotification);
    }

    private void saveFailedNotification(Notification notification, NotificationStatus status) {
        var failedNotification = Notification.createFailed(
            notification.getSenderType(),
            notification.getSenderId(),
            notification.getReceiverId(),
            notification.getType(),
            notification.getTitle(),
            notification.getBody(),
            status
        );
        save(failedNotification);
    }

    private void save(Notification notification) {
        notificationCommandRepository.save(notification);
    }
}
