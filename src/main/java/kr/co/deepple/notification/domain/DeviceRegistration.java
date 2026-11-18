package kr.co.deepple.notification.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
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

  private Instant statusChangedAt;

  private DeviceRegistration(
      long memberId, String deviceId, String registrationToken, Instant statusChangedAt) {
    this.memberId = memberId;
    this.deviceId = deviceId;
    this.registrationToken = registrationToken;
    this.statusChangedAt = statusChangedAt;
  }

  public static DeviceRegistration of(
      long memberId, String deviceId, String registrationToken, @NonNull Instant registeredAt) {
    return new DeviceRegistration(memberId, deviceId, registrationToken, registeredAt);
  }

  public void update(
      @NonNull String deviceId, @NonNull String registrationToken, @NonNull Instant updatedAt) {
    this.deviceId = deviceId;
    this.registrationToken = registrationToken;
    activate(updatedAt);
  }

  public void activate(@NonNull Instant changedAt) {
    if (statusChangedAt != null && !changedAt.isAfter(statusChangedAt)) {
      return;
    }
    isActive = true;
    statusChangedAt = changedAt;
  }

  public void deactivate(@NonNull Instant changedAt) {
    if (statusChangedAt != null && !changedAt.isAfter(statusChangedAt)) {
      return;
    }
    isActive = false;
    statusChangedAt = changedAt;
  }
}
