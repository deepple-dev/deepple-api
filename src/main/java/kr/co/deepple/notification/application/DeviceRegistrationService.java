package kr.co.deepple.notification.application;

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
    deviceRegistrationRepository
        .findByMemberId(memberId)
        .ifPresentOrElse(
            device -> device.update(request.deviceId(), request.registrationToken()),
            () ->
                deviceRegistrationRepository.save(
                    DeviceRegistration.of(
                        memberId, request.deviceId(), request.registrationToken())));
  }

  @Transactional
  public void activateByMemberId(long memberId) {
    deviceRegistrationRepository.findByMemberId(memberId).ifPresent(DeviceRegistration::activate);
  }

  @Transactional
  public void deactivateByMemberId(long memberId) {
    deviceRegistrationRepository
        .findByMemberIdAndIsActiveTrue(memberId)
        .ifPresent(DeviceRegistration::deactivate);
  }
}
