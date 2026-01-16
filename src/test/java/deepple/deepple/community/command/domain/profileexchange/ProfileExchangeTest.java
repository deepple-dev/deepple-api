package deepple.deepple.community.command.domain.profileexchange;

import deepple.deepple.common.event.Events;
import deepple.deepple.community.command.domain.profileexchange.exception.InvalidProfileExchangeStatusException;
import deepple.deepple.community.command.domain.profileexchange.exception.SelfProfileExchangeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;

class ProfileExchangeTest {

    @Test
    @DisplayName("자기 자신에게 프로필 교환 요청을 한 경우, 예외 발생")
    void throwExceptionWhenRequesterIdIsEqualToResponderId() {
        // Given
        long requestId = 1L;
        String senderName = "sender";

        // When & Then
        try (MockedStatic<Events> eventsMock = mockStatic(Events.class)) {
            assertThatThrownBy(() -> ProfileExchange.request(requestId, requestId, senderName))
                .isInstanceOf(SelfProfileExchangeException.class);
        }
    }

    @Test
    @DisplayName("프로필 교환을 신청할 경우, 신청 대기 상태로 생성한다.")
    void createProfileExchangeWithWaitingStatus() {
        // Given
        long requesterId = 1L;
        long responderId = 3L;
        String senderName = "sender";

        // When & Then
        try (MockedStatic<Events> eventsMock = mockStatic(Events.class)) {
            ProfileExchange profileExchange = ProfileExchange.request(requesterId, responderId, senderName);

            assertThat(profileExchange.getRequesterId()).isEqualTo(requesterId);
            assertThat(profileExchange.getResponderId()).isEqualTo(responderId);
            assertThat(profileExchange.getStatus()).isEqualTo(ProfileExchangeStatus.WAITING);
        }
    }

    @Nested
    @DisplayName("프로필 교환 응답")
    class Respond {

        @Test
        @DisplayName("이미 완료된 요청에 응답할 경우, 예외 발생")
        void throwsExceptionWhenStatusIsNotWaiting() {
            // Given
            String requesterName = "requester";
            String responderName = "responder";

            try (MockedStatic<Events> eventsMock = mockStatic(Events.class)) {
                ProfileExchange profileExchange = ProfileExchange.request(1L, 2L, requesterName);
                profileExchange.approve(responderName);

                // When & Then
                assertThatThrownBy(() -> profileExchange.approve(responderName))
                    .isInstanceOf(InvalidProfileExchangeStatusException.class);
            }
        }

        @Test
        @DisplayName("수락")
        void approve() {
            // Given
            String requesterName = "requester";
            String responderName = "responder";

            try (MockedStatic<Events> eventsMock = mockStatic(Events.class)) {
                ProfileExchange profileExchange = ProfileExchange.request(1L, 2L, requesterName);

                // When
                profileExchange.approve(responderName);

                // Then
                assertThat(profileExchange.getStatus()).isEqualTo(ProfileExchangeStatus.APPROVE);
            }
        }

        @Test
        @DisplayName("거절")
        void reject() {
            // Given
            String requesterName = "requester";
            String responderName = "responder";

            try (MockedStatic<Events> eventsMock = mockStatic(Events.class)) {
                ProfileExchange profileExchange = ProfileExchange.request(1L, 2L, requesterName);

                // When
                profileExchange.reject(responderName);

                // Then
                assertThat(profileExchange.getStatus()).isEqualTo(ProfileExchangeStatus.REJECTED);
            }
        }
    }
}
