package kr.co.deepple.notification.command.application;

import kr.co.deepple.notification.command.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationDataReader {

    public static final String TEMPLATE_CACHE = "notificationTemplate";

    private final NotificationTemplateCommandRepository notificationTemplateRepository;
    private final NotificationPreferenceCommandRepository notificationPreferenceRepository;
    private final DeviceRegistrationCommandRepository deviceRegistrationCommandRepository;

    @Cacheable(value = TEMPLATE_CACHE, key = "#type", unless = "#result == null")
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