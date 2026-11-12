package kr.co.deepple.notification.command.application;

import kr.co.deepple.notification.command.domain.DeviceRegistration;
import kr.co.deepple.notification.command.domain.DeviceRegistrationCommandRepository;
import kr.co.deepple.notification.presentation.DeviceRegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceRegistrationService {

  private final DeviceRegistrationCommandRepository deviceRegistrationCommandRepository;

  @Transactional
  public void register(long memberId, DeviceRegisterRequest request) {
    deviceRegistrationCommandRepository
        .findByMemberId(memberId)
        .ifPresentOrElse(
            device -> device.update(request.deviceId(), request.registrationToken()),
            () ->
                deviceRegistrationCommandRepository.save(
                    DeviceRegistration.of(
                        memberId, request.deviceId(), request.registrationToken())));
  }

  @Transactional
  public void activateByMemberId(long memberId) {
    deviceRegistrationCommandRepository
        .findByMemberId(memberId)
        .ifPresent(DeviceRegistration::activate);
  }

  @Transactional
  public void deactivateByMemberId(long memberId) {
    deviceRegistrationCommandRepository
        .findByMemberIdAndIsActiveTrue(memberId)
        .ifPresent(DeviceRegistration::deactivate);
  }
}
