package kr.co.deepple.community.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.community.fixture.ProfileExchangeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProfileExchangeTest {

  @Test
  @DisplayName("자기 자신에게 프로필 교환 요청을 한 경우, 예외 발생")
  void throwExceptionWhenRequesterIdIsEqualToResponderId() {
    // Given
    long requestId = 1L;

    // When & Then
    assertThatThrownBy(() -> ProfileExchange.request(requestId, requestId))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", CommunityErrorCode.SELF_PROFILE_EXCHANGE);
  }

  @Test
  @DisplayName("프로필 교환을 신청할 경우, 신청 대기 상태로 생성한다.")
  void createProfileExchangeWithWaitingStatus() {
    // Given
    long requesterId = 1L;
    long responderId = 3L;

    // When & Then
    ProfileExchange profileExchange = ProfileExchange.request(requesterId, responderId);

    assertThat(profileExchange.getRequesterId()).isEqualTo(requesterId);
    assertThat(profileExchange.getResponderId()).isEqualTo(responderId);
    assertThat(profileExchange.getStatus()).isEqualTo(ProfileExchangeStatus.WAITING);
  }

  @Nested
  @DisplayName("프로필 교환 응답")
  class Respond {

    @Test
    @DisplayName("이미 완료된 요청에 응답할 경우, 예외 발생")
    void throwsExceptionWhenStatusIsNotWaiting() {
      // Given
      ProfileExchange profileExchange =
          ProfileExchangeFixture.builder().status(ProfileExchangeStatus.APPROVE).build();

      // When & Then
      assertThatThrownBy(() -> profileExchange.approve())
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", CommunityErrorCode.INVALID_PROFILE_EXCHANGE_STATUS);
    }

    @Test
    @DisplayName("수락")
    void approve() {
      // Given
      ProfileExchange profileExchange = ProfileExchangeFixture.builder().build();

      // When
      profileExchange.approve();

      // Then
      assertThat(profileExchange.getStatus()).isEqualTo(ProfileExchangeStatus.APPROVE);
    }

    @Test
    @DisplayName("거절")
    void reject() {
      // Given
      ProfileExchange profileExchange = ProfileExchangeFixture.builder().build();

      // When
      profileExchange.reject();

      // Then
      assertThat(profileExchange.getStatus()).isEqualTo(ProfileExchangeStatus.REJECTED);
    }
  }
}
