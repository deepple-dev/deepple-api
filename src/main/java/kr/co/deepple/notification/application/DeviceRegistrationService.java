package kr.co.deepple.notification.application;

import java.time.Instant;
import kr.co.deepple.notification.application.dto.DeviceRegisterRequest;
import kr.co.deepple.notification.application.port.out.DeviceRegistrationRepository;
import kr.co.deepple.notification.domain.DeviceRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceRegistrationService {

  private final DeviceRegistrationRepository deviceRegistrationRepository;

  @Transactional
  public void register(long memberId, DeviceRegisterRequest request) {
    Instant now = Instant.now();
    deviceRegistrationRepository
        .findByMemberIdForUpdate(memberId)
        .ifPresentOrElse(
            device -> device.update(request.deviceId(), request.registrationToken(), now),
            () ->
                deviceRegistrationRepository.save(
                    DeviceRegistration.of(
                        memberId, request.deviceId(), request.registrationToken(), now)));
  }

  @Transactional
  public void activateByMemberId(long memberId, Instant changedAt) {
    deviceRegistrationRepository
        .findByMemberIdForUpdate(memberId)
        .ifPresent(device -> device.activate(changedAt));
  }

  @Transactional
  public void deactivateByMemberId(long memberId, Instant changedAt) {
    deviceRegistrationRepository
        .findByMemberIdForUpdate(memberId)
        .ifPresent(device -> device.deactivate(changedAt));
  }
}
