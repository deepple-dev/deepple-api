package kr.co.deepple.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import kr.co.deepple.notification.application.port.in.MemberNotificationSetting;
import kr.co.deepple.notification.application.port.out.NotificationPreferenceQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindMemberNotificationSettingServiceTest {

  @Mock private NotificationPreferenceQueryRepository notificationPreferenceQueryRepository;

  @InjectMocks private FindMemberNotificationSettingService findMemberNotificationSettingService;

  @Test
  @DisplayName("회원의 전역 알림 설정 값을 돌려준다.")
  void findMemberNotificationSetting() {
    given(notificationPreferenceQueryRepository.findEnabledGloballyByMemberId(1L))
        .willReturn(Optional.of(false));

    assertThat(findMemberNotificationSettingService.findMemberNotificationSetting(1L))
        .contains(new MemberNotificationSetting(1L, false));
  }

  @Test
  @DisplayName("알림 설정이 없는 회원은 빈 값을 돌려준다.")
  void returnEmptyWhenNoPreference() {
    given(notificationPreferenceQueryRepository.findEnabledGloballyByMemberId(1L))
        .willReturn(Optional.empty());

    assertThat(findMemberNotificationSettingService.findMemberNotificationSetting(1L)).isEmpty();
  }
}
