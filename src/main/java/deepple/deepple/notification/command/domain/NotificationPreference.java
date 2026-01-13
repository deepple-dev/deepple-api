package deepple.deepple.notification.command.domain;

import deepple.deepple.common.entity.SoftDeleteBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.EnumMap;
import java.util.Map;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(
    name = "notification_preferences",
    indexes = {@Index(name = "idx_notification_preferences_member_id", columnList = "memberId")}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationPreference extends SoftDeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    private Long memberId;

    @Getter
    private boolean isEnabledGlobally = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "member_notification_preferences", joinColumns = @JoinColumn(name = "member_id"))
    @MapKeyEnumerated(STRING)
    @MapKeyColumn(name = "notification_type")
    @Column(name = "is_enabled")
    private Map<NotificationType, Boolean> preferences = new EnumMap<>(NotificationType.class);

    private NotificationPreference(Long memberId, Map<NotificationType, Boolean> preferences) {
        this.memberId = memberId;
        this.preferences = new EnumMap<>(preferences);
    }

    public static NotificationPreference of(long memberId) {
        Map<NotificationType, Boolean> defaults = new EnumMap<>(NotificationType.class);
        for (NotificationType type : NotificationType.values()) {
            defaults.put(type, true);
        }
        return new NotificationPreference(memberId, defaults);
    }

    public Map<NotificationType, Boolean> getNotificationPreferences() {
        return new EnumMap<>(preferences);
    }

    public boolean canReceive(NotificationType type) {
        return isEnabledGlobally && preferences.getOrDefault(type, false);
    }

    public void enableGlobally() {
        isEnabledGlobally = true;
    }

    public void disableGlobally() {
        isEnabledGlobally = false;
    }

    public boolean isDisabledForType(NotificationType type) {
        return !preferences.getOrDefault(type, false);
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
}
