package kr.co.deepple.notification.command.application;

import java.util.Optional;
import kr.co.deepple.notification.command.domain.DeviceRegistration;
import kr.co.deepple.notification.command.domain.DeviceRegistrationCommandRepository;
import kr.co.deepple.notification.command.domain.NotificationPreference;
import kr.co.deepple.notification.command.domain.NotificationPreferenceCommandRepository;
import kr.co.deepple.notification.command.domain.NotificationTemplateCommandRepository;
import kr.co.deepple.notification.command.domain.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    return notificationTemplateRepository.findByType(type).map(NotificationTemplateInfo::from);
  }

  @Transactional(readOnly = true)
  public Optional<NotificationPreference> findPreference(long memberId) {
    return notificationPreferenceRepository.findByMemberId(memberId);
  }

  @Transactional(readOnly = true)
  public Optional<DeviceRegistration> findActiveDevice(long memberId) {
    return deviceRegistrationCommandRepository.findByMemberIdAndIsActiveTrue(memberId);
  }
}
