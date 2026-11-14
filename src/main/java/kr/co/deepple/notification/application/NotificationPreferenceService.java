package kr.co.deepple.notification.application;

import static kr.co.deepple.notification.application.NotificationTypeMapper.toNotificationType;

import java.util.Map;
import java.util.stream.Collectors;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.notification.application.dto.NotificationPreferenceSetRequest;
import kr.co.deepple.notification.application.dto.NotificationPreferenceToggleRequest;
import kr.co.deepple.notification.application.port.out.NotificationPreferenceRepository;
import kr.co.deepple.notification.domain.NotificationErrorCode;
import kr.co.deepple.notification.domain.NotificationPreference;
import kr.co.deepple.notification.domain.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {

  private final NotificationPreferenceRepository notificationPreferenceRepository;

  @Transactional
  public void createIfAbsent(long memberId) {
    if (notificationPreferenceRepository.existsByMemberId(memberId)) {
      return;
    }
    notificationPreferenceRepository.save(NotificationPreference.from(memberId));
  }

  @Transactional
  public void changeGloballyIfPresent(long memberId, boolean enabled) {
    notificationPreferenceRepository
        .findByMemberId(memberId)
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
    getNotificationPreference(memberId)
        .enableForNotificationType(toNotificationType(request.type()));
  }

  @Transactional
  public void disableGlobally(long memberId) {
    getNotificationPreference(memberId).disableGlobally();
  }

  @Transactional
  public void disableForType(long memberId, NotificationPreferenceToggleRequest request) {
    getNotificationPreference(memberId)
        .disableForNotificationType(toNotificationType(request.type()));
  }

  @Transactional
  public void setNotificationPreferences(long memberId, NotificationPreferenceSetRequest request) {
    NotificationPreference preference = getNotificationPreference(memberId);

    Map<NotificationType, Boolean> preferences =
        request.preferences().entrySet().stream()
            .collect(
                Collectors.toMap(entry -> toNotificationType(entry.getKey()), Map.Entry::getValue));

    preference.updateNotificationPreferences(preferences);
  }

  private NotificationPreference getNotificationPreference(long memberId) {
    return notificationPreferenceRepository
        .findByMemberId(memberId)
        .orElseThrow(() ->
            new DeeppleException(
                NotificationErrorCode.NOTIFICATION_PREFERENCE_NOT_FOUND, "memberId=" + memberId));
  }
}
