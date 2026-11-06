package deepple.deepple.report.command.application;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.report.command.domain.Report;
import deepple.deepple.report.command.domain.ReportCommandRepository;
import deepple.deepple.report.command.domain.ReportErrorCode;
import deepple.deepple.report.presentation.dto.ReportResultUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.Optional;

import static deepple.deepple.report.command.domain.ReportResult.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceTest {

    @Mock
    private ReportCommandRepository reportCommandRepository;

    @InjectMocks
    private AdminReportService adminReportService;

    @Nested
    @DisplayName("updateResult 메서드 테스트")
    class UpdateResultTests {
        private final long reportId = 1L;
        private final long adminId = 100L;
        private final long version = 0L;
        private Report mockReport;

        private void stubFindReport() {
            mockReport = mock(Report.class);
            when(reportCommandRepository.findById(reportId))
                .thenReturn(Optional.of(mockReport));
        }

        @Test
        @DisplayName("정상 케이스: 결과가 REJECTED일 때 report.reject 호출")
        void shouldRejectReportWhenResultIsRejected() {
            // given
            stubFindReport();
            var request = new ReportResultUpdateRequest(version, REJECTED.name());
            when(mockReport.hasVersionConflict(version)).thenReturn(false);

            // when
            adminReportService.updateResult(reportId, request, adminId);

            // then
            verify(mockReport).reject(adminId);
            verify(mockReport, never()).warn(anyLong());
            verify(reportCommandRepository, never()).save(any());
        }

        @Test
        @DisplayName("정상 케이스: 결과가 WARNED일 때 report.warn 호출")
        void shouldWarnReportWhenResultIsWarned() {
            // given
            stubFindReport();
            var request = new ReportResultUpdateRequest(version, WARNED.name());
            when(mockReport.hasVersionConflict(version)).thenReturn(false);

            // when
            adminReportService.updateResult(reportId, request, adminId);

            // then
            verify(mockReport).warn(adminId);
            verify(mockReport, never()).reject(anyLong());
            verify(reportCommandRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 케이스: PENDING 결과는 설정 불가")
        void shouldThrowInvalidReportResultExceptionWhenResultIsPending() {
            // given
            stubFindReport();
            var request = new ReportResultUpdateRequest(version, PENDING.name());
            when(mockReport.hasVersionConflict(version)).thenReturn(false);

            // when & then
            assertThatThrownBy(() ->
                adminReportService.updateResult(reportId, request, adminId))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", ReportErrorCode.INVALID_REPORT_RESULT)
                .hasMessageContaining("PENDING 으로 결과를 설정할 수 없습니다.");

            verify(mockReport, never()).reject(anyLong());
            verify(mockReport, never()).warn(anyLong());
            verify(reportCommandRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 케이스: 알 수 없는 결과는 설정 불가")
        void shouldThrowInvalidReportResultExceptionWhenResultIsUnknown() {
            // given
            stubFindReport();
            var request = new ReportResultUpdateRequest(version, "UNKNOWN");
            when(mockReport.hasVersionConflict(version)).thenReturn(false);

            // when & then
            assertThatThrownBy(() ->
                adminReportService.updateResult(reportId, request, adminId))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", ReportErrorCode.INVALID_REPORT_RESULT)
                .hasMessageContaining("Invalid report result: UNKNOWN");

            verify(mockReport, never()).reject(anyLong());
            verify(mockReport, never()).warn(anyLong());
            verify(reportCommandRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 케이스: 버전 충돌 시 OptimisticLockingFailureException 발생")
        void shouldThrowOptimisticLockingFailureExceptionWhenVersionConflict() {
            // given
            stubFindReport();
            var request = new ReportResultUpdateRequest(version, REJECTED.name());
            when(mockReport.hasVersionConflict(version)).thenReturn(true);

            // when & then
            assertThatThrownBy(() ->
                adminReportService.updateResult(reportId, request, adminId))
                .isInstanceOf(OptimisticLockingFailureException.class)
                .hasMessageContaining("신고를 처리할 수 없습니다.");

            verify(mockReport, never()).reject(anyLong());
            verify(mockReport, never()).warn(anyLong());
            verify(reportCommandRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 케이스: 존재하지 않는 리포트 ID면 REPORT_NOT_FOUND 예외발생")
        void shouldThrowReportNotFoundWhenReportNotFound() {
            // given
            when(reportCommandRepository.findById(reportId))
                .thenReturn(Optional.empty());
            var request = new ReportResultUpdateRequest(version, REJECTED.name());

            // when & then
            assertThatThrownBy(() ->
                adminReportService.updateResult(reportId, request, adminId))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", ReportErrorCode.REPORT_NOT_FOUND);

            verify(reportCommandRepository).findById(reportId);
            verify(reportCommandRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 케이스: null 요청 객체 전달 시 NPE 발생")
        void shouldThrowNullPointerExceptionWhenRequestIsNull() {
            // given
            stubFindReport();

            // when & then
            assertThatThrownBy(() ->
                adminReportService.updateResult(reportId, null, adminId))
                .isInstanceOf(NullPointerException.class);

            verify(reportCommandRepository).findById(reportId);
            verify(mockReport, never()).hasVersionConflict(anyLong());
            verify(reportCommandRepository, never()).save(any());
        }
    }
}
