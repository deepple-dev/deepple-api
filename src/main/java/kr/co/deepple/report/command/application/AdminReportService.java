package kr.co.deepple.report.command.application;

import jakarta.transaction.Transactional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.report.command.domain.Report;
import kr.co.deepple.report.command.domain.ReportCommandRepository;
import kr.co.deepple.report.command.domain.ReportErrorCode;
import kr.co.deepple.report.command.domain.ReportResult;
import kr.co.deepple.report.presentation.dto.ReportResultUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminReportService {

  private final ReportCommandRepository reportCommandRepository;

  @Transactional
  public void updateResult(long reportId, ReportResultUpdateRequest request, long adminId) {
    Report report = getReport(reportId);
    validateReport(report, request.version());
    setReportResult(report, request, adminId);
  }

  private Report getReport(long id) {
    return reportCommandRepository
        .findById(id)
        .orElseThrow(
            () -> new DeeppleException(ReportErrorCode.REPORT_NOT_FOUND, "reportId=" + id));
  }

  private void validateReport(Report report, long version) {
    if (report.hasVersionConflict(version)) {
      throw new OptimisticLockingFailureException("신고를 처리할 수 없습니다.");
    }
  }

  private void setReportResult(Report report, ReportResultUpdateRequest request, long adminId) {
    ReportResult reportResult = ReportResult.from(request.result());
    switch (reportResult) {
      case REJECTED -> report.reject(adminId);
      case WARNED -> report.warn(adminId);
      case PENDING ->
          throw new DeeppleException(
              ReportErrorCode.INVALID_REPORT_RESULT, "PENDING 으로 결과를 설정할 수 없습니다.");
      default ->
          throw new DeeppleException(
              ReportErrorCode.INVALID_REPORT_RESULT, "result=" + request.result());
    }
  }
}
