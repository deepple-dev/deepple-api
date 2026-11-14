package kr.co.deepple.notification.application;

import java.util.Optional;
import kr.co.deepple.notification.application.port.in.FindMemberNotificationSettingUseCase;
import kr.co.deepple.notification.application.port.in.MemberNotificationSetting;
import kr.co.deepple.notification.application.port.out.NotificationPreferenceQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindMemberNotificationSettingService implements FindMemberNotificationSettingUseCase {

  private final NotificationPreferenceQueryRepository notificationPreferenceQueryRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<MemberNotificationSetting> findMemberNotificationSetting(long memberId) {
    return notificationPreferenceQueryRepository
        .findEnabledGloballyByMemberId(memberId)
        .map(enabledGlobally -> new MemberNotificationSetting(memberId, enabledGlobally));
  }
}
