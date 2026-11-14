package kr.co.deepple.notification.adapter.out.persistence;

import static kr.co.deepple.notification.domain.NotificationType.LIKE;
import static kr.co.deepple.notification.domain.NotificationType.MATCH_REQUEST;
import static kr.co.deepple.notification.domain.NotificationType.PROFILE_EXCHANGE_REQUEST;
import static kr.co.deepple.notification.domain.NotificationType.values;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.notification.application.dto.NotificationPreferenceView;
import kr.co.deepple.notification.fixture.NotificationPreferenceFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

class NotificationPreferenceQueryRepositoryTest extends RepositoryTestSupport {

  @Autowired private TestEntityManager em;

  @Autowired private NotificationPreferenceQueryRepository repository;

  @Test
  @DisplayName("findByMemberId(): 존재하는 회원의 알림 설정 조회")
  void findByMemberIdExistingMember() {
    // given
    var preference = NotificationPreferenceFixture.builder().memberId(1L).build();
    preference.disableForNotificationType(LIKE);
    preference.enableForNotificationType(MATCH_REQUEST);
    em.persist(preference);
    em.flush();
    em.clear();

    // when
    Optional<NotificationPreferenceView> result = repository.findByMemberId(1L);

    // then
    assertThat(result).isPresent();

    NotificationPreferenceView view = result.get();
    assertThat(view.memberId()).isEqualTo(1L);
    assertThat(view.isEnabledGlobally()).isTrue();
    assertThat(view.preferences()).containsEntry("LIKE", false);
    assertThat(view.preferences()).containsEntry("MATCH_REQUEST", true);
  }

  @Test
  @DisplayName("findByMemberId(): 존재하지 않는 회원의 알림 설정 조회")
  void findByMemberIdNonExistingMember() {
    // given
    var preference = NotificationPreferenceFixture.builder().memberId(1L).build();
    em.persist(preference);
    em.flush();
    em.clear();

    // when
    Optional<NotificationPreferenceView> result = repository.findByMemberId(999L);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("findByMemberId(): 전역 알림이 비활성화된 경우")
  void findByMemberIdGloballyDisabled() {
    // given
    var preference =
        NotificationPreferenceFixture.builder().memberId(2L).enabledGlobally(false).build();
    preference.enableForNotificationType(LIKE);
    em.persist(preference);
    em.flush();
    em.clear();

    // when
    Optional<NotificationPreferenceView> result = repository.findByMemberId(2L);

    // then
    assertThat(result).isPresent();

    NotificationPreferenceView view = result.get();
    assertThat(view.memberId()).isEqualTo(2L);
    assertThat(view.isEnabledGlobally()).isFalse();
    assertThat(view.preferences()).containsEntry("LIKE", true);
  }

  @Test
  @DisplayName("findByMemberId(): 기본 설정 확인 (모든 알림 활성화)")
  void findByMemberIdDefaultSettings() {
    // given
    var preference = NotificationPreferenceFixture.builder().memberId(3L).build();
    em.persist(preference);
    em.flush();
    em.clear();

    // when
    Optional<NotificationPreferenceView> result = repository.findByMemberId(3L);

    // then
    assertThat(result).isPresent();

    NotificationPreferenceView view = result.get();
    assertThat(view.memberId()).isEqualTo(3L);
    assertThat(view.isEnabledGlobally()).isTrue();

    for (var notificationType : values()) {
      assertThat(view.preferences()).containsEntry(notificationType.name(), true);
    }
  }

  @Test
  @DisplayName("findByMemberId(): 일부 알림 타입만 비활성화된 경우")
  void findByMemberIdPartiallyDisabled() {
    // given
    var preference = NotificationPreferenceFixture.builder().memberId(4L).build();
    preference.disableForNotificationType(LIKE);
    preference.enableForNotificationType(MATCH_REQUEST);
    preference.enableForNotificationType(PROFILE_EXCHANGE_REQUEST);
    em.persist(preference);
    em.flush();
    em.clear();

    // when
    Optional<NotificationPreferenceView> result = repository.findByMemberId(4L);

    // then
    assertThat(result).isPresent();

    NotificationPreferenceView view = result.get();
    assertThat(view.memberId()).isEqualTo(4L);
    assertThat(view.isEnabledGlobally()).isTrue();

    assertThat(view.preferences()).containsEntry("LIKE", false);

    assertThat(view.preferences()).containsEntry("MATCH_REQUEST", true);
    assertThat(view.preferences()).containsEntry("PROFILE_EXCHANGE_REQUEST", true);
  }
}
