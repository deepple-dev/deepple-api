package kr.co.deepple.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import kr.co.deepple.notification.fixture.DeviceRegistrationFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeviceRegistrationTest {

  private static final Instant CHANGED_AT = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  @DisplayName("of(): registrationToken 초기화 및 기본 활성화 상태")
  void ofInitializesFields() {
    // given
    long userId = 1L;
    String deviceId = "device-123";
    String token = "reg-token-xyz";

    // when
    var reg = DeviceRegistration.of(userId, deviceId, token, CHANGED_AT);

    // then
    assertThat(reg.getRegistrationToken()).isEqualTo(token);
    assertThat(reg.isActive()).isTrue();
    assertThat(reg.getStatusChangedAt()).isEqualTo(CHANGED_AT);
  }

  @Test
  @DisplayName("deactivate(): 마지막 변경보다 새로우면 비활성화하고 변경 시각을 남긴다")
  void deactivateSetsInactive() {
    // given
    var reg = DeviceRegistrationFixture.builder().statusChangedAt(CHANGED_AT).build();
    Instant later = CHANGED_AT.plusSeconds(1L);

    // when
    reg.deactivate(later);

    // then
    assertThat(reg.isActive()).isFalse();
    assertThat(reg.getStatusChangedAt()).isEqualTo(later);
  }

  @Test
  @DisplayName("activate(): 마지막 변경보다 새로우면 활성화한다")
  void activateRestoresActive() {
    // given
    var reg = DeviceRegistrationFixture.builder().active(false).statusChangedAt(CHANGED_AT).build();

    // when
    reg.activate(CHANGED_AT.plusSeconds(1L));

    // then
    assertThat(reg.isActive()).isTrue();
  }

  @Test
  @DisplayName("activate()·deactivate(): 마지막 변경보다 오래되거나 같은 시각이면 무시한다")
  void ignoreStaleChange() {
    // given
    var reg = DeviceRegistrationFixture.builder().active(true).statusChangedAt(CHANGED_AT).build();

    // when
    reg.deactivate(CHANGED_AT.minusSeconds(1L));
    reg.deactivate(CHANGED_AT);

    // then
    assertThat(reg.isActive()).isTrue();
    assertThat(reg.getStatusChangedAt()).isEqualTo(CHANGED_AT);
  }

  @Test
  @DisplayName("activate(): 변경 시각이 없는 기존 행은 반영한다")
  void applyWhenNoChangedAt() {
    // given
    var reg = DeviceRegistrationFixture.builder().active(false).statusChangedAt(null).build();

    // when
    reg.activate(CHANGED_AT);

    // then
    assertThat(reg.isActive()).isTrue();
    assertThat(reg.getStatusChangedAt()).isEqualTo(CHANGED_AT);
  }

  @Test
  @DisplayName("update(): 디바이스 ID와 토큰을 갱신하고 활성화한다")
  void updateUpdatesDeviceIdAndTokenAndCallsActivate() {
    // given
    var reg =
        DeviceRegistrationFixture.builder()
            .deviceId("old-device")
            .registrationToken("old-token")
            .active(false)
            .statusChangedAt(CHANGED_AT)
            .build();
    String newDeviceId = "new-device-456";
    String newToken = "new-token-xyz";

    // when
    reg.update(newDeviceId, newToken, CHANGED_AT.plusSeconds(1L));

    // then
    assertThat(reg.getDeviceId()).isEqualTo(newDeviceId);
    assertThat(reg.getRegistrationToken()).isEqualTo(newToken);
    assertThat(reg.isActive()).isTrue();
  }
}
