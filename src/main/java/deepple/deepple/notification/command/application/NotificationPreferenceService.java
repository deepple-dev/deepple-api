package deepple.deepple.notification.command.application;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.notification.command.domain.NotificationErrorCode;
import deepple.deepple.notification.command.domain.NotificationPreference;
import deepple.deepple.notification.command.domain.NotificationPreferenceCommandRepository;
import deepple.deepple.notification.command.domain.NotificationType;
import deepple.deepple.notification.presentation.NotificationPreferenceSetRequest;
import deepple.deepple.notification.presentation.NotificationPreferenceToggleRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

import static deepple.deepple.notification.command.application.NotificationTypeMapper.toNotificationType;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {

    private final NotificationPreferenceCommandRepository notificationPreferenceRepository;

    @Transactional
    public void createIfAbsent(long memberId) {
        if (notificationPreferenceRepository.existsByMemberId(memberId)) {
            return;
        }
        notificationPreferenceRepository.save(NotificationPreference.of(memberId));
    }

    @Transactional
    public void changeGloballyIfPresent(long memberId, boolean enabled) {
        notificationPreferenceRepository.findByMemberId(memberId)
            .ifPresent(preference -> {
                if (enabled) {
                    preference.enableGlobally();
                } else {
                    preference.disableGlobally();
                }
            });
    }

    @Transactional
    public void enableGlobally(long memberId) {
        getNotificationPreference(memberId).enableGlobally();
    }

    @Transactional
    public void enableForType(long memberId, NotificationPreferenceToggleRequest request) {
        getNotificationPreference(memberId).enableForNotificationType(toNotificationType(request.type()));
    }

    @Transactional
    public void disableGlobally(long memberId) {
        getNotificationPreference(memberId).disableGlobally();
    }

    @Transactional
    public void disableForType(long memberId, NotificationPreferenceToggleRequest request) {
        getNotificationPreference(memberId).disableForNotificationType(toNotificationType(request.type()));
    }

    @Transactional
    public void setNotificationPreferences(Long memberId, NotificationPreferenceSetRequest request) {
        var preference = getNotificationPreference(memberId);

        Map<NotificationType, Boolean> preferences = request.preferences().entrySet().stream()
            .collect(Collectors.toMap(
                entry -> toNotificationType(entry.getKey()),
                Map.Entry::getValue
            ));

        preference.updateNotificationPreferences(preferences);
    }

    private NotificationPreference getNotificationPreference(long memberId) {
        return notificationPreferenceRepository.findByMemberId(memberId)
            .orElseThrow(() -> new DeeppleException(
                NotificationErrorCode.NOTIFICATION_PREFERENCE_NOT_FOUND,
                "memberId=" + memberId
            ));
    }
}
