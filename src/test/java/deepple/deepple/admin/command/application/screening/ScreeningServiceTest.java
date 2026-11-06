package deepple.deepple.admin.command.application.screening;

import deepple.deepple.admin.command.domain.AdminErrorCode;
import deepple.deepple.admin.command.domain.screening.RejectionReasonType;
import deepple.deepple.admin.command.domain.screening.Screening;
import deepple.deepple.admin.command.domain.screening.ScreeningCommandRepository;
import deepple.deepple.admin.presentation.screening.ScreeningApproveRequest;
import deepple.deepple.admin.presentation.screening.ScreeningRejectRequest;
import deepple.deepple.common.exception.DeeppleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScreeningServiceTest {

    @Mock
    private ScreeningCommandRepository screeningCommandRepository;

    @InjectMocks
    private ScreeningService screeningService;

    @Nested
    @DisplayName("createIfAbsent() 테스트")
    class CreateIfAbsent {

        @Test
        @DisplayName("심사가 없으면 Screening을 생성하고 저장합니다.")
        void createNewMemberScreening() {
            // given
            long memberId = 100L;
            when(screeningCommandRepository.existsByMemberId(memberId)).thenReturn(false);

            // when
            screeningService.createIfAbsent(memberId);

            // then
            verify(screeningCommandRepository).save(any(Screening.class));
        }

        @Test
        @DisplayName("이미 심사가 있으면 저장하지 않습니다.")
        void skipWhenScreeningExists() {
            // given
            long memberId = 100L;
            when(screeningCommandRepository.existsByMemberId(memberId)).thenReturn(true);

            // when
            screeningService.createIfAbsent(memberId);

            // then
            verify(screeningCommandRepository, never()).save(any(Screening.class));
        }
    }

    @Nested
    @DisplayName("rescreen() 테스트")
    class Rescreen {

        @Test
        @DisplayName("재심사 요청 시 새로운 Screening을 생성합니다.")
        void rescreenCreatesNewScreening() {
            // given
            long memberId = 100L;

            // when
            screeningService.rescreen(memberId);

            // then
            verify(screeningCommandRepository).save(any(Screening.class));
        }
    }

    @Nested
    @DisplayName("approve() 테스트")
    class Approve {

        @Test
        @DisplayName("존재하는 Screening이면 approve(adminId)를 호출합니다.")
        void approveWhenScreeningExists() {
            // given
            long screeningId = 1L;
            long adminId = 999L;

            Screening screening = mock(Screening.class);
            when(screeningCommandRepository.findById(screeningId)).thenReturn(Optional.of(screening));

            // when
            screeningService.approve(screeningId, new ScreeningApproveRequest(1L), adminId);

            // then
            verify(screening).approve(adminId);
        }

        @Test
        @DisplayName("존재하지 않는 Screening이면 SCREENING_NOT_FOUND 예외를 던집니다.")
        void throwExceptionWhenScreeningDoesNotExist() {
            // given
            long screeningId = 1L;
            when(screeningCommandRepository.findById(screeningId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> screeningService.approve(screeningId, new ScreeningApproveRequest(1L), 999L))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.SCREENING_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("reject() 테스트")
    class Reject {

        @Test
        @DisplayName("존재하는 Screening이면 reject(adminId, rejectionReason)을 호출합니다.")
        void rejectWhenScreeningExists() {
            // given
            long screeningId = 1L;
            long adminId = 999L;

            Screening screening = mock(Screening.class);
            when(screeningCommandRepository.findById(screeningId)).thenReturn(Optional.of(screening));

            String rejectionReason = "STOLEN_IMAGE";
            ScreeningRejectRequest request = new ScreeningRejectRequest(rejectionReason, 1L);

            // when
            screeningService.reject(screeningId, adminId, request);

            // then
            verify(screening).reject(adminId, RejectionReasonType.STOLEN_IMAGE);
        }

        @Test
        @DisplayName("존재하지 않는 Screening이면 SCREENING_NOT_FOUND 예외를 던집니다.")
        void throwExceptionWhenRejectingNonExistentScreening() {
            // given
            long screeningId = 1L;
            when(screeningCommandRepository.findById(screeningId)).thenReturn(Optional.empty());

            ScreeningRejectRequest request = new ScreeningRejectRequest("STOLEN_IMAGE", 1L);

            // when & then
            assertThatThrownBy(() -> screeningService.reject(screeningId, 999L, request))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.SCREENING_NOT_FOUND);
        }

        @Test
        @DisplayName("존재하지 않는 반려 사유인 경우 INVALID_REJECTION_REASON 예외를 던집니다.")
        void throwExceptionWhenRejectionReasonNonExists() {
            // given
            long screeningId = 1L;
            Screening screening = mock(Screening.class);
            when(screeningCommandRepository.findById(screeningId)).thenReturn(Optional.of(screening));

            ScreeningRejectRequest request = new ScreeningRejectRequest("NON_EXISTING_REASON", 1L);

            // when & then
            assertThatThrownBy(() -> screeningService.reject(screeningId, 999L, request))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.INVALID_REJECTION_REASON);
        }
    }
}

