package kr.co.deepple.notification.application.port.out;

import java.util.Optional;
import kr.co.deepple.notification.domain.DeviceRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRegistrationRepository extends JpaRepository<DeviceRegistration, Long> {
  Optional<DeviceRegistration> findByMemberIdAndDeviceId(long memberId, String deviceId);

  Optional<DeviceRegistration> findByMemberIdAndIsActiveTrue(long memberId);

  Optional<DeviceRegistration> findByMemberId(long memberId);

  void deleteByMemberId(long memberId);
}
