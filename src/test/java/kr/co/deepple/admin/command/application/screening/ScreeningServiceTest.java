package kr.co.deepple.admin.command.application.screening;

import kr.co.deepple.admin.command.domain.AdminErrorCode;
import kr.co.deepple.admin.command.domain.screening.RejectionReasonType;
import kr.co.deepple.admin.command.domain.screening.Screening;
import kr.co.deepple.admin.command.domain.screening.ScreeningCommandRepository;
import kr.co.deepple.admin.command.domain.screening.ScreeningStatus;
import kr.co.deepple.admin.fixture.ScreeningFixture;
import kr.co.deepple.admin.presentation.screening.ScreeningApproveRequest;
import kr.co.deepple.admin.presentation.screening.ScreeningRejectRequest;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.lock.DistributedLockTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScreeningServiceTest {

    @Mock
    private ScreeningCommandRepository screeningCommandRepository;

    @Mock
    private DistributedLockTemplate distributedLockTemplate;

    @InjectMocks
    private ScreeningService screeningService;

    @BeforeEach
    void setUp() {
        lenient().doAnswer(invocation -> {
            invocation.<Runnable>getArgument(1).run();
            return null;
        }).when(distributedLockTemplate).executeInTransaction(anyString(), any(Runnable.class));
    }

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
            verify(distributedLockTemplate).executeInTransaction(eq("screening:" + memberId), any(Runnable.class));
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
        @DisplayName("대기 중인 심사가 없으면 회원별 락 안에서 새 Screening을 생성합니다.")
        void rescreenCreatesNewScreening() {
            // given
            long memberId = 100L;
            when(screeningCommandRepository.existsByMemberIdAndStatus(memberId, ScreeningStatus.PENDING))
                .thenReturn(false);

            // when
            screeningService.rescreen(memberId);

            // then
            verify(distributedLockTemplate).executeInTransaction(eq("screening:" + memberId), any(Runnable.class));
            verify(screeningCommandRepository).save(any(Screening.class));
        }

        @Test
        @DisplayName("대기 중인 심사가 있으면 새로 만들지 않습니다.")
        void skipWhenPendingScreeningExists() {
            // given
            long memberId = 100L;
            when(screeningCommandRepository.existsByMemberIdAndStatus(memberId, ScreeningStatus.PENDING))
                .thenReturn(true);

            // when
            screeningService.rescreen(memberId);

            // then
            verify(screeningCommandRepository, never()).save(any(Screening.class));
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

            Screening screening = ScreeningFixture.builder().id(screeningId).version(1L).build();
            when(screeningCommandRepository.findById(screeningId)).thenReturn(Optional.of(screening));

            // when
            screeningService.approve(screeningId, new ScreeningApproveRequest(1L), adminId);

            // then
            assertThat(screening.getStatus()).isEqualTo(ScreeningStatus.APPROVED);
            assertThat(screening.getAdminId()).isEqualTo(adminId);
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

            Screening screening = ScreeningFixture.builder().id(screeningId).version(1L).build();
            when(screeningCommandRepository.findById(screeningId)).thenReturn(Optional.of(screening));

            String rejectionReason = "STOLEN_IMAGE";
            ScreeningRejectRequest request = new ScreeningRejectRequest(rejectionReason, 1L);

            // when
            screeningService.reject(screeningId, adminId, request);

            // then
            assertThat(screening.getStatus()).isEqualTo(ScreeningStatus.REJECTED);
            assertThat(screening.getAdminId()).isEqualTo(adminId);
            assertThat(screening.getRejectionReason()).isEqualTo(RejectionReasonType.STOLEN_IMAGE);
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
            Screening screening = ScreeningFixture.builder().id(screeningId).version(1L).build();
            when(screeningCommandRepository.findById(screeningId)).thenReturn(Optional.of(screening));

            ScreeningRejectRequest request = new ScreeningRejectRequest("NON_EXISTING_REASON", 1L);

            // when & then
            assertThatThrownBy(() -> screeningService.reject(screeningId, 999L, request))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.INVALID_REJECTION_REASON);
        }
    }
}

