package kr.co.deepple.notification.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "device_registrations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceRegistration {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long memberId;

  private String deviceId;

  private String registrationToken;

  private boolean isActive = true;

  private DeviceRegistration(long memberId, String deviceId, String registrationToken) {
    this.memberId = memberId;
    this.deviceId = deviceId;
    this.registrationToken = registrationToken;
  }

  public static DeviceRegistration of(long memberId, String deviceId, String registrationToken) {
    return new DeviceRegistration(memberId, deviceId, registrationToken);
  }

  public void update(@NonNull String deviceId, @NonNull String registrationToken) {
    this.deviceId = deviceId;
    this.registrationToken = registrationToken;
    activate();
  }

  public void refreshRegistrationToken(@NonNull String registrationToken) {
    this.registrationToken = registrationToken;
    isActive = true;
  }

  public void activate() {
    isActive = true;
  }

  public void deactivate() {
    isActive = false;
  }
}
