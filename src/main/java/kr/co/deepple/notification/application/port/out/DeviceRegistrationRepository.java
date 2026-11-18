package kr.co.deepple.notification.application.port.out;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import kr.co.deepple.notification.domain.DeviceRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface DeviceRegistrationRepository extends JpaRepository<DeviceRegistration, Long> {
  Optional<DeviceRegistration> findByMemberIdAndDeviceId(long memberId, String deviceId);

  Optional<DeviceRegistration> findByMemberIdAndIsActiveTrue(long memberId);

  Optional<DeviceRegistration> findByMemberId(long memberId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT d FROM DeviceRegistration d WHERE d.memberId = :memberId")
  Optional<DeviceRegistration> findByMemberIdForUpdate(long memberId);

  void deleteByMemberId(long memberId);
}
