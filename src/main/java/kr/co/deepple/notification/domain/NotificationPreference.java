package kr.co.deepple.notification.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Table;
import java.util.EnumMap;
import java.util.Map;
import kr.co.deepple.common.entity.SoftDeleteBaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_preferences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationPreference extends SoftDeleteBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long memberId;

  private boolean isEnabledGlobally = true;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "member_notification_preferences",
      joinColumns = @JoinColumn(name = "member_id"))
  @MapKeyEnumerated(EnumType.STRING)
  @MapKeyColumn(name = "notification_type")
  @Column(name = "is_enabled")
  @Getter(AccessLevel.NONE)
  private Map<NotificationType, Boolean> preferences = new EnumMap<>(NotificationType.class);

  private NotificationPreference(long memberId, Map<NotificationType, Boolean> preferences) {
    this.memberId = memberId;
    this.preferences = new EnumMap<>(preferences);
  }

  public static NotificationPreference from(long memberId) {
    Map<NotificationType, Boolean> defaults = new EnumMap<>(NotificationType.class);
    for (NotificationType type : NotificationType.values()) {
      defaults.put(type, true);
    }
    return new NotificationPreference(memberId, defaults);
  }

  public Map<NotificationType, Boolean> getNotificationPreferences() {
    Map<NotificationType, Boolean> result = new EnumMap<>(NotificationType.class);
    for (NotificationType type : NotificationType.values()) {
      result.put(type, isEnabledForType(type));
    }
    return result;
  }

  public boolean canReceive(NotificationType type) {
    return isEnabledGlobally && isEnabledForType(type);
  }

  public void enableGlobally() {
    isEnabledGlobally = true;
  }

  public void disableGlobally() {
    isEnabledGlobally = false;
  }

  public boolean isDisabledForType(NotificationType type) {
    return !isEnabledForType(type);
  }

  public void enableForNotificationType(NotificationType type) {
    preferences.put(type, true);
  }

  public void disableForNotificationType(NotificationType type) {
    preferences.put(type, false);
  }

  public void updateNotificationPreferences(Map<NotificationType, Boolean> preferences) {
    this.preferences.putAll(preferences);
  }

  private boolean isEnabledForType(NotificationType type) {
    return preferences.getOrDefault(type, true);
  }
}
