package deepple.deepple.notification.command.application;

import deepple.deepple.notification.command.domain.DeviceRegistration;
import deepple.deepple.notification.command.domain.DeviceRegistrationCommandRepository;
import deepple.deepple.notification.presentation.DeviceRegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceRegistrationService {

    private final DeviceRegistrationCommandRepository deviceRegistrationCommandRepository;

    /**
     * 디바이스를 등록하거나 토큰을 갱신합니다.
     * 현재 단일 기기만 지원하므로, 기존 디바이스가 있으면 갱신하고 없으면 새로 등록합니다.
     *
     * @param memberId 회원 ID
     * @param request  디바이스 등록 요청 (deviceId: 기기 고유 식별자, registrationToken: FCM 토큰)
     */
    @Transactional
    public void register(long memberId, DeviceRegisterRequest request) {
        deviceRegistrationCommandRepository.findByMemberId(memberId)
            .ifPresentOrElse(
                device -> device.update(request.deviceId(), request.registrationToken()),
                () -> deviceRegistrationCommandRepository.save(
                    DeviceRegistration.of(memberId, request.deviceId(), request.registrationToken()))
            );
    }

    /**
     * 회원의 디바이스 등록을 활성화합니다.
     * 계정 복구 시 호출됩니다.
     */
    @Transactional
    public void activateByMemberId(long memberId) {
        deviceRegistrationCommandRepository.findByMemberId(memberId)
            .ifPresent(DeviceRegistration::activate);
    }

    /**
     * 회원의 활성화된 디바이스 등록을 비활성화합니다.
     * 로그아웃이나 휴면 상태 전환 시 호출됩니다.
     */
    @Transactional
    public void deactivateByMemberId(long memberId) {
        deviceRegistrationCommandRepository.findByMemberIdAndIsActiveTrue(memberId)
            .ifPresent(DeviceRegistration::deactivate);
    }
}
