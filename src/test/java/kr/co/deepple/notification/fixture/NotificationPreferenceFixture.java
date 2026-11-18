package kr.co.deepple.notification.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.time.Instant;
import kr.co.deepple.notification.domain.NotificationPreference;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationPreferenceFixture {

  private Long id;
  private long memberId = 1L;
  private boolean enabledGlobally = true;
  private Instant globalSettingChangedAt;

  public static NotificationPreferenceFixture builder() {
    return new NotificationPreferenceFixture();
  }

  public NotificationPreference build() {
    NotificationPreference preference = NotificationPreference.from(memberId);
    setField(preference, "id", id);
    setField(preference, "isEnabledGlobally", enabledGlobally);
    setField(preference, "globalSettingChangedAt", globalSettingChangedAt);
    return preference;
  }
}
