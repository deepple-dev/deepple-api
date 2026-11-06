package deepple.deepple.admin.command.domain.screening;

import deepple.deepple.admin.command.domain.AdminErrorCode;
import deepple.deepple.admin.command.domain.screening.event.ScreeningApprovedEvent;
import deepple.deepple.common.event.Events;
import deepple.deepple.common.exception.DeeppleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

class ScreeningTest {

    @Nested
    @DisplayName("from() 메서드 테스트")
    class FromMethodTest {

        @Test
        @DisplayName("주어진 memberId로 from()을 호출하면, PENDING 상태의 Screening을 생성합니다.")
        void createPendingScreening() {
            // given
            Long memberId = 1L;

            // when
            Screening screening = Screening.from(memberId);

            // then
            assertThat(screening.getMemberId()).isEqualTo(memberId);
            assertThat(screening.getStatus()).isEqualTo(ScreeningStatus.PENDING);
            assertThat(screening.getAdminId()).isNull();
            assertThat(screening.getRejectionReason()).isNull();
        }
    }

    @Nested
    @DisplayName("approve() 메서드 테스트")
    class ApproveMethodTest {

        @Test
        @DisplayName("PENDING 상태인 Screening을 approve하면, APPROVED 상태가 되며 거절 사유는 null이 됩니다.")
        void approvePendingScreening() {
            // given
            final long memberId = 10L;
            Screening screening = Screening.from(memberId);

            // when
            try (MockedStatic<Events> eventsMockedStatic = mockStatic(Events.class)) {
                screening.approve(1L);

                // then
                eventsMockedStatic.verify(() ->
                    Events.raise(argThat((ScreeningApprovedEvent event) ->
                        event.getMemberId() == memberId
                    )), times(1));
            }
            assertThat(screening.getStatus()).isEqualTo(ScreeningStatus.APPROVED);
            assertThat(screening.getAdminId()).isEqualTo(1L);
            assertThat(screening.getRejectionReason()).isNull();
        }

        @Test
        @DisplayName("REJECTED 상태인 Screening을 approve하면, 다시 APPROVED 상태가 되며 거절 사유는 null이 됩니다.")
        void approveRejectedScreening() {
            // given
            final long memberId = 10L;
            Screening screening = Screening.from(memberId);
            screening.reject(1L, RejectionReasonType.CONTACT_IN_PROFILE);

            // when
            try (MockedStatic<Events> eventsMockedStatic = mockStatic(Events.class)) {
                screening.approve(2L);

                // then
                eventsMockedStatic.verify(() ->
                    Events.raise(argThat((ScreeningApprovedEvent event) ->
                        event.getMemberId() == memberId
                    )), times(1));
            }
            assertThat(screening.getStatus()).isEqualTo(ScreeningStatus.APPROVED);
            assertThat(screening.getAdminId()).isEqualTo(2L); // adminId 갱신
            assertThat(screening.getRejectionReason()).isNull();
        }

        @Test
        @DisplayName("APPROVED 상태인 Screening을 다시 approve하면, 상태 변화 없이 adminId만 갱신되고 거절 사유는 여전히 null입니다.")
        void approveApprovedScreening() {
            // given
            final long memberId = 10L;
            Screening screening = Screening.from(memberId);
            screening.approve(1L);

            try (MockedStatic<Events> eventsMockedStatic = mockStatic(Events.class)) {
                screening.approve(2L);

                // then
                eventsMockedStatic.verify(() ->
                    Events.raise(argThat((ScreeningApprovedEvent event) ->
                        event.getMemberId() == memberId
                    )), times(1));
            }
            assertThat(screening.getStatus()).isEqualTo(ScreeningStatus.APPROVED);
            assertThat(screening.getAdminId()).isEqualTo(2L);
            assertThat(screening.getRejectionReason()).isNull();
        }
    }

    @Nested
    @DisplayName("reject() 메서드 테스트")
    class RejectMethodTest {

        @Test
        @DisplayName("PENDING 상태인 Screening을 reject하면, REJECTED 상태가 되며 거절 사유가 설정됩니다.")
        void rejectPendingScreening() {
            // given
            Screening screening = Screening.from(2L);

            // when
            screening.reject(3L, RejectionReasonType.INAPPROPRIATE_IMAGE);

            // then
            assertThat(screening.getStatus()).isEqualTo(ScreeningStatus.REJECTED);
            assertThat(screening.getAdminId()).isEqualTo(3L);
            assertThat(screening.getRejectionReason()).isEqualTo(RejectionReasonType.INAPPROPRIATE_IMAGE);
        }

        @Test
        @DisplayName("REJECTED 상태인 Screening을 다시 reject하면, 재거절이 가능하며 거절 사유가 덮어씌워집니다.")
        void rejectRejectedScreening() {
            // given
            Screening screening = Screening.from(2L);
            screening.reject(3L, RejectionReasonType.INAPPROPRIATE_IMAGE);

            // when
            screening.reject(4L, RejectionReasonType.CONTACT_IN_PROFILE);

            // then
            assertThat(screening.getStatus()).isEqualTo(ScreeningStatus.REJECTED);
            assertThat(screening.getAdminId()).isEqualTo(4L);
            assertThat(screening.getRejectionReason()).isEqualTo(RejectionReasonType.CONTACT_IN_PROFILE);
        }

        @Test
        @DisplayName("APPROVED 상태인 Screening을 reject하려고 하면 예외가 발생합니다.")
        void rejectApprovedScreeningThrowsException() {
            // given
            Screening screening = Screening.from(2L);
            screening.approve(10L);

            // when & then
            assertThatThrownBy(() -> screening.reject(11L, RejectionReasonType.CONTACT_IN_PROFILE))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.CANNOT_REJECT_APPROVED_SCREENING);
        }
    }
}
