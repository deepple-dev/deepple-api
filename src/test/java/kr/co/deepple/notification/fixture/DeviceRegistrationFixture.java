package kr.co.deepple.notification.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import kr.co.deepple.notification.command.domain.DeviceRegistration;
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

  public static DeviceRegistrationFixture builder() {
    return new DeviceRegistrationFixture();
  }

  public DeviceRegistration build() {
    DeviceRegistration registration = DeviceRegistration.of(memberId, deviceId, registrationToken);
    setField(registration, "id", id);
    setField(registration, "isActive", active);
    return registration;
  }
}
