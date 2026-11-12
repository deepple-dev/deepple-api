package kr.co.deepple.notification.command.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRegistrationCommandRepository
    extends JpaRepository<DeviceRegistration, Long> {
  Optional<DeviceRegistration> findByMemberIdAndDeviceId(long memberId, String deviceId);

  Optional<DeviceRegistration> findByMemberIdAndIsActiveTrue(long memberId);

  Optional<DeviceRegistration> findByMemberId(long memberId);

  void deleteByMemberId(long memberId);
}
