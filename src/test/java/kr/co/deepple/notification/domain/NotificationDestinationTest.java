package kr.co.deepple.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NotificationDestinationTest {

  @ParameterizedTest
  @CsvSource({
    "MATCH_REQUEST, MESSAGE",
    "MATCH_ACCEPT, MESSAGE",
    "MATCH_REJECT, MESSAGE",
    "PROFILE_EXCHANGE_REQUEST, MESSAGE",
    "PROFILE_EXCHANGE_ACCEPT, MESSAGE",
    "PROFILE_EXCHANGE_REJECT, MESSAGE",
    "LIKE, PROFILE",
    "PROFILE_IMAGE_CHANGE_REQUEST, PROFILE_EDIT",
    "INAPPROPRIATE_PROFILE, PROFILE_EDIT",
    "INAPPROPRIATE_PROFILE_IMAGE, PROFILE_EDIT",
    "INTERVIEW_WRITE_REQUEST, INTERVIEW",
    "INAPPROPRIATE_INTERVIEW, INTERVIEW",
    "HEART_GRANTED, HEART_HISTORY",
    "INAPPROPRIATE_SELF_INTRODUCTION, NONE",
    "REPORT, NONE",
    "SCREENING_APPROVED, NONE",
    "SCREENING_REJECTED, NONE",
    "INACTIVITY_REMINDER, NONE"
  })
  @DisplayName("알림 유형마다 이동할 곳을 정한다.")
  void mapTypeToDestination(NotificationType type, NotificationDestination expected) {
    assertThat(NotificationDestination.of(type)).isEqualTo(expected);
  }

  @Test
  @DisplayName("회원이 보낸 알림이 상대 회원으로 이동하면 보낸 회원 ID를, 아니면 null을 이동 대상 ID로 쓴다.")
  void destinationIdOnlyForMemberDestination() {
    assertThat(NotificationDestination.MESSAGE.destinationId(SenderType.MEMBER, 7L)).isEqualTo(7L);
    assertThat(NotificationDestination.PROFILE.destinationId(SenderType.MEMBER, 7L)).isEqualTo(7L);
    assertThat(NotificationDestination.PROFILE.destinationId(SenderType.ADMIN, 7L)).isNull();
    assertThat(NotificationDestination.HEART_HISTORY.destinationId(SenderType.MEMBER, 7L)).isNull();
  }
}
