package kr.co.deepple.notification.application.port.out;

import java.util.Optional;
import kr.co.deepple.notification.application.dto.NotificationPreferenceView;

public interface NotificationPreferenceQueryRepository {
  Optional<NotificationPreferenceView> findByMemberId(long memberId);

  Optional<Boolean> findEnabledGloballyByMemberId(long memberId);
}
