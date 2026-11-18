package kr.co.deepple.notification.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import kr.co.deepple.notification.application.dto.DeviceRegisterRequest;
import kr.co.deepple.notification.application.port.out.DeviceRegistrationRepository;
import kr.co.deepple.notification.fixture.DeviceRegistrationFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeviceRegistrationServiceTest {

  private static final Instant CHANGED_AT = Instant.parse("2026-01-01T00:00:00Z");

  @Mock DeviceRegistrationRepository repository;

  @InjectMocks DeviceRegistrationService service;

  @Test
  @DisplayName("신규 디바이스 등록 시 새로운 DeviceRegistration 저장")
  void registerSavesNewRegistrationWhenNotExists() {
    // given
    long memberId = 1L;
    var request = new DeviceRegisterRequest("device-123", "token-abc");
    when(repository.findByMemberIdForUpdate(memberId)).thenReturn(Optional.empty());

    // when
    service.register(memberId, request);

    // then
    verify(repository)
        .save(
            argThat(reg ->
                reg.getRegistrationToken().equals(request.registrationToken())
                    && reg.getDeviceId().equals(request.deviceId())
                    && reg.isActive()));
  }

  @Test
  @DisplayName("기존 디바이스 존재 시 update 메서드 호출")
  void registerUpdatesWhenDeviceExists() {
    // given
    long memberId = 2L;
    var request = new DeviceRegisterRequest("new-device-456", "new-token");
    var existing =
        spy(
            DeviceRegistrationFixture.builder()
                .memberId(memberId)
                .deviceId("old-device")
                .registrationToken("old-token")
                .build());
    when(repository.findByMemberIdForUpdate(memberId)).thenReturn(Optional.of(existing));

    // when
    service.register(memberId, request);

    // then
    verify(existing).update(eq(request.deviceId()), eq(request.registrationToken()), any());
    verify(repository, never()).save(any());
  }

  @Test
  @DisplayName("activateByMemberId(): 디바이스가 있으면 이벤트 발생 시각으로 활성화를 요청한다")
  void activateByMemberIdActivatesExistingDevice() {
    // given
    long memberId = 3L;
    var existing = spy(DeviceRegistrationFixture.builder().memberId(memberId).build());
    when(repository.findByMemberIdForUpdate(memberId)).thenReturn(Optional.of(existing));

    // when
    service.activateByMemberId(memberId, CHANGED_AT);

    // then
    verify(existing).activate(CHANGED_AT);
  }

  @Test
  @DisplayName("deactivateByMemberId(): 디바이스가 있으면 이벤트 발생 시각으로 비활성화를 요청한다")
  void deactivateByMemberIdDeactivatesDevice() {
    // given
    long memberId = 5L;
    var existing = spy(DeviceRegistrationFixture.builder().memberId(memberId).build());
    when(repository.findByMemberIdForUpdate(memberId)).thenReturn(Optional.of(existing));

    // when
    service.deactivateByMemberId(memberId, CHANGED_AT);

    // then
    verify(existing).deactivate(CHANGED_AT);
  }
}
