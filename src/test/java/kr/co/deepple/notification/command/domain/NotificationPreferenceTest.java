package kr.co.deepple.notification.command.domain;

import static kr.co.deepple.notification.command.domain.NotificationType.LIKE;
import static kr.co.deepple.notification.command.domain.NotificationType.MATCH_ACCEPT;
import static kr.co.deepple.notification.command.domain.NotificationType.MATCH_REQUEST;
import static kr.co.deepple.notification.command.domain.NotificationType.REPORT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.EnumMap;
import java.util.Map;
import kr.co.deepple.notification.fixture.NotificationPreferenceFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationPreferenceTest {

  @Test
  @DisplayName("of() 초기화: 글로벌과 타입별 수신 모두 활성화")
  void ofInitializesDefaults() {
    // given
    long memberId = 42L;

    // when
    var preference = NotificationPreference.from(memberId);

    // then
    assertThat(preference.getMemberId()).isEqualTo(memberId);
    assertThat(preference.isEnabledGlobally()).isTrue();
    assertThat(preference.canReceive(MATCH_REQUEST)).isTrue();
    assertThat(preference.canReceive(LIKE)).isTrue();
  }

  @Test
  @DisplayName("disableGlobally(): 글로벌 비활성화 시 모든 타입 수신 불가")
  void disableGloballyDisablesAllTypes() {
    // given
    var preference = NotificationPreferenceFixture.builder().build();

    // when
    preference.disableGlobally();

    // then
    assertThat(preference.isEnabledGlobally()).isFalse();
    assertThat(preference.canReceive(MATCH_REQUEST)).isFalse();
    assertThat(preference.canReceive(LIKE)).isFalse();
  }

  @Test
  @DisplayName("enableGlobally(): 글로벌 활성화 시 원복 확인")
  void enableGloballyRestoresAllTypes() {
    // given
    var preference = NotificationPreferenceFixture.builder().enabledGlobally(false).build();

    // when
    preference.enableGlobally();

    // then
    assertThat(preference.isEnabledGlobally()).isTrue();
    assertThat(preference.canReceive(MATCH_REQUEST)).isTrue();
    assertThat(preference.canReceive(LIKE)).isTrue();
  }

  @Test
  @DisplayName("disableForNotificationType(): 특정 타입만 비활성화")
  void disableForNotificationTypeAffectsOnlyThatType() {
    // given
    var preference = NotificationPreferenceFixture.builder().build();

    // when
    preference.disableForNotificationType(MATCH_REQUEST);

    // then
    assertThat(preference.canReceive(MATCH_REQUEST)).isFalse();
    assertThat(preference.canReceive(LIKE)).isTrue();
  }

  @Test
  @DisplayName("enableForNotificationType(): 특정 타입만 활성화")
  void enableForNotificationTypeRestoresOnlyThatType() {
    // given
    var preference = NotificationPreferenceFixture.builder().build();
    preference.disableForNotificationType(MATCH_REQUEST);

    // when
    preference.enableForNotificationType(MATCH_REQUEST);

    // then
    assertThat(preference.canReceive(MATCH_REQUEST)).isTrue();
  }

  @Test
  @DisplayName("isDisabledForType(): 내부 맵 값 그대로 반환")
  void isDisabledForTypeReflectsInternalMap() {
    // given
    var preference = NotificationPreferenceFixture.builder().build();
    boolean before = preference.isDisabledForType(MATCH_REQUEST);

    // when
    preference.disableForNotificationType(MATCH_REQUEST);
    boolean after = preference.isDisabledForType(MATCH_REQUEST);

    // then
    assertThat(before).isFalse();
    assertThat(after).isTrue();
  }

  @Test
  @DisplayName("getNotificationPreferences(): 반환된 맵은 내부 상태의 복사본이어야 함")
  void getNotificationPreferencesReturnsCopy() {
    // given
    var preference = NotificationPreferenceFixture.builder().build();

    // when
    Map<NotificationType, Boolean> returnedPreferences = preference.getNotificationPreferences();
    returnedPreferences.put(MATCH_REQUEST, false);

    // then
    assertThat(returnedPreferences.get(MATCH_REQUEST)).isFalse();
    assertThat(preference.canReceive(MATCH_REQUEST)).isTrue();
  }

  @Test
  @DisplayName("updateNotificationPreferences(): 지정된 타입만 업데이트")
  void updateNotificationPreferencesUpdatesSpecifiedTypes() {
    // given
    var preference = NotificationPreferenceFixture.builder().build();
    Map<NotificationType, Boolean> updates = new EnumMap<>(NotificationType.class);
    updates.put(MATCH_REQUEST, false);
    updates.put(LIKE, false);

    // when
    preference.updateNotificationPreferences(updates);

    // then
    assertThat(preference.canReceive(MATCH_REQUEST)).isFalse();
    assertThat(preference.canReceive(LIKE)).isFalse();
    assertThat(preference.canReceive(MATCH_ACCEPT)).isTrue();
  }

  @Test
  @DisplayName("설정에 없는 타입: 수신 가능으로 판단")
  void missingTypeIsEnabled() {
    // given
    var preference = NotificationPreferenceFixture.builder().build();
    setField(preference, "preferences", new EnumMap<>(Map.of(MATCH_REQUEST, false)));

    // when & then
    assertThat(preference.canReceive(REPORT)).isTrue();
    assertThat(preference.isDisabledForType(REPORT)).isFalse();
    assertThat(preference.canReceive(MATCH_REQUEST)).isFalse();
  }

  @Test
  @DisplayName("getNotificationPreferences(): 설정에 없는 타입도 수신 가능으로 포함")
  void getNotificationPreferencesIncludesMissingTypes() {
    // given
    var preference = NotificationPreferenceFixture.builder().build();
    setField(preference, "preferences", new EnumMap<>(Map.of(MATCH_REQUEST, false)));

    // when
    Map<NotificationType, Boolean> result = preference.getNotificationPreferences();

    // then
    assertThat(result).hasSize(NotificationType.values().length);
    assertThat(result.get(REPORT)).isTrue();
    assertThat(result.get(MATCH_REQUEST)).isFalse();
  }
}
