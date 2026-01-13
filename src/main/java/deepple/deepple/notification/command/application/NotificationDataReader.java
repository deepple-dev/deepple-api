package deepple.deepple.notification.command.application;

import deepple.deepple.notification.command.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationDataReader {

    private final NotificationTemplateCommandRepository notificationTemplateRepository;
    private final NotificationPreferenceCommandRepository notificationPreferenceRepository;
    private final DeviceRegistrationCommandRepository deviceRegistrationCommandRepository;

    @Cacheable(value = "notificationTemplate", key = "#type")
    @Transactional(readOnly = true)
    public Optional<NotificationTemplateInfo> findTemplate(NotificationType type) {
        return notificationTemplateRepository.findByType(type)
            .map(NotificationTemplateInfo::from);
    }

    @Transactional(readOnly = true)
    public Optional<NotificationPreference> findPreference(Long memberId) {
        return notificationPreferenceRepository.findByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public Optional<DeviceRegistration> findActiveDevice(Long memberId) {
        return deviceRegistrationCommandRepository.findByMemberIdAndIsActiveTrue(memberId);
    }
}