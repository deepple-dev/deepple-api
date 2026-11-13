package kr.co.deepple.report.command.application;

import static kr.co.deepple.report.command.domain.ReportResult.PENDING;
import static kr.co.deepple.report.command.domain.ReportResult.REJECTED;
import static kr.co.deepple.report.command.domain.ReportResult.WARNED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.report.command.domain.Report;
import kr.co.deepple.report.command.domain.ReportCommandRepository;
import kr.co.deepple.report.command.domain.ReportErrorCode;
import kr.co.deepple.report.event.ReportWarnedEvent;
import kr.co.deepple.report.fixture.ReportFixture;
import kr.co.deepple.report.presentation.dto.ReportResultUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceTest {

  @Mock private ReportCommandRepository reportCommandRepository;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private AdminReportService adminReportService;

  @Nested
  @DisplayName("updateResult 메서드 테스트")
  class UpdateResultTests {

    private final long reportId = 1L;
    private final long adminId = 100L;
    private final long version = 0L;
    private Report report;

    private void stubFindReport() {
      stubFindReport(version);
    }

    private void stubFindReport(long reportVersion) {
      report = ReportFixture.builder().id(reportId).version(reportVersion).build();
      when(reportCommandRepository.findById(reportId)).thenReturn(Optional.of(report));
    }

    @Test
    @DisplayName("정상 케이스: 결과가 REJECTED일 때 report.reject 호출")
    void shouldRejectReportWhenResultIsRejected() {
      // given
      stubFindReport();
      var request = new ReportResultUpdateRequest(version, REJECTED.name());

      // when
      adminReportService.updateResult(reportId, request, adminId);

      // then
      Mockito.verify(eventPublisher, Mockito.never()).publishEvent(Mockito.any(Object.class));
      assertThat(report.getResult()).isEqualTo(REJECTED);
      assertThat(report.getAdminId()).isEqualTo(adminId);
      verify(reportCommandRepository, never()).save(any());
    }

    @Test
    @DisplayName("정상 케이스: 결과가 WARNED일 때 report.warn 호출")
    void shouldWarnReportWhenResultIsWarned() {
      // given
      stubFindReport();
      var request = new ReportResultUpdateRequest(version, WARNED.name());

      // when
      adminReportService.updateResult(reportId, request, adminId);

      // then
      Mockito.verify(eventPublisher).publishEvent(Mockito.any(ReportWarnedEvent.class));
      assertThat(report.getResult()).isEqualTo(WARNED);
      assertThat(report.getAdminId()).isEqualTo(adminId);
      verify(reportCommandRepository, never()).save(any());
    }

    @Test
    @DisplayName("예외 케이스: PENDING 결과는 설정 불가")
    void shouldThrowInvalidReportResultExceptionWhenResultIsPending() {
      // given
      stubFindReport();
      var request = new ReportResultUpdateRequest(version, PENDING.name());

      // when & then
      assertThatThrownBy(() -> adminReportService.updateResult(reportId, request, adminId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", ReportErrorCode.INVALID_REPORT_RESULT)
          .hasMessageContaining("PENDING 으로 결과를 설정할 수 없습니다.");

      assertThat(report.getResult()).isEqualTo(PENDING);
      assertThat(report.getAdminId()).isNull();
      verify(reportCommandRepository, never()).save(any());
    }

    @Test
    @DisplayName("예외 케이스: 알 수 없는 결과는 설정 불가")
    void shouldThrowInvalidReportResultExceptionWhenResultIsUnknown() {
      // given
      stubFindReport();
      var request = new ReportResultUpdateRequest(version, "UNKNOWN");

      // when & then
      assertThatThrownBy(() -> adminReportService.updateResult(reportId, request, adminId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", ReportErrorCode.INVALID_REPORT_RESULT)
          .hasMessageContaining("result=UNKNOWN");

      assertThat(report.getResult()).isEqualTo(PENDING);
      assertThat(report.getAdminId()).isNull();
      verify(reportCommandRepository, never()).save(any());
    }

    @Test
    @DisplayName("예외 케이스: 버전 충돌 시 OptimisticLockingFailureException 발생")
    void shouldThrowOptimisticLockingFailureExceptionWhenVersionConflict() {
      // given
      stubFindReport(version + 1);
      var request = new ReportResultUpdateRequest(version, REJECTED.name());

      // when & then
      assertThatThrownBy(() -> adminReportService.updateResult(reportId, request, adminId))
          .isInstanceOf(OptimisticLockingFailureException.class)
          .hasMessageContaining("신고를 처리할 수 없습니다.");

      assertThat(report.getResult()).isEqualTo(PENDING);
      assertThat(report.getAdminId()).isNull();
      verify(reportCommandRepository, never()).save(any());
    }

    @Test
    @DisplayName("예외 케이스: 존재하지 않는 리포트 ID면 REPORT_NOT_FOUND 예외발생")
    void shouldThrowReportNotFoundWhenReportNotFound() {
      // given
      when(reportCommandRepository.findById(reportId)).thenReturn(Optional.empty());
      var request = new ReportResultUpdateRequest(version, REJECTED.name());

      // when & then
      assertThatThrownBy(() -> adminReportService.updateResult(reportId, request, adminId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", ReportErrorCode.REPORT_NOT_FOUND);

      verify(reportCommandRepository).findById(reportId);
      verify(reportCommandRepository, never()).save(any());
    }

    @Test
    @DisplayName("예외 케이스: null 요청 객체 전달 시 NPE 발생")
    void shouldThrowNullPointerExceptionWhenRequestIsNull() {
      // given
      report = spy(ReportFixture.builder().id(reportId).version(version).build());
      when(reportCommandRepository.findById(reportId)).thenReturn(Optional.of(report));

      // when & then
      assertThatThrownBy(() -> adminReportService.updateResult(reportId, null, adminId))
          .isInstanceOf(NullPointerException.class);

      verify(reportCommandRepository).findById(reportId);
      verify(report, never()).hasVersionConflict(anyLong());
      assertThat(report.getResult()).isEqualTo(PENDING);
      verify(reportCommandRepository, never()).save(any());
    }
  }
}
