package kr.co.deepple.report.command.application;

import jakarta.transaction.Transactional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import kr.co.deepple.report.command.domain.Report;
import kr.co.deepple.report.command.domain.ReportCommandRepository;
import kr.co.deepple.report.command.domain.ReportErrorCode;
import kr.co.deepple.report.command.domain.ReportReasonType;
import kr.co.deepple.report.event.ReportCreatedEvent;
import kr.co.deepple.report.presentation.dto.ReportCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

  private final ReportCommandRepository reportCommandRepository;
  private final FindMemberSummaryUseCase findMemberSummaryUseCase;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void report(ReportCreateRequest request, long reporterId) {
    validateRequest(request, reporterId);
    ReportReasonType reportReasonType = ReportReasonType.from(request.reason());
    Report report =
        Report.of(reporterId, request.reporteeId(), reportReasonType, request.content());
    reportCommandRepository.save(report);
    eventPublisher.publishEvent(ReportCreatedEvent.of(reporterId, request.reporteeId()));
  }

  private void validateRequest(ReportCreateRequest request, long reporterId) {
    findMemberSummaryUseCase.getMemberSummary(request.reporteeId());
    if (reportCommandRepository.existsByReporterIdAndReporteeId(reporterId, request.reporteeId())) {
      throw new DeeppleException(
          ReportErrorCode.REPORT_ALREADY_EXISTS,
          "reporterId=" + reporterId + ", reporteeId=" + request.reporteeId());
    }
  }
}
