package kr.co.deepple.notification.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.time.Instant;
import kr.co.deepple.notification.domain.DeviceRegistration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeviceRegistrationFixture {

  private Long id;
  private long memberId = 1L;
  private String deviceId = "device-id";
  private String registrationToken = "registration-token";
  private boolean active = true;
  private Instant statusChangedAt = Instant.parse("2026-01-01T00:00:00Z");

  public static DeviceRegistrationFixture builder() {
    return new DeviceRegistrationFixture();
  }

  public DeviceRegistration build() {
    DeviceRegistration registration =
        DeviceRegistration.of(memberId, deviceId, registrationToken, Instant.EPOCH);
    setField(registration, "id", id);
    setField(registration, "isActive", active);
    setField(registration, "statusChangedAt", statusChangedAt);
    return registration;
  }
}
