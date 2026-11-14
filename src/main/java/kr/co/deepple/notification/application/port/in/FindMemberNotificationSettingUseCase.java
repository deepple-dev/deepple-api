package kr.co.deepple.notification.application.port.in;

import java.util.Optional;

public interface FindMemberNotificationSettingUseCase {
  Optional<MemberNotificationSetting> findMemberNotificationSetting(long memberId);
}
