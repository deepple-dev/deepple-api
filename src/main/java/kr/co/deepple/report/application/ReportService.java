package kr.co.deepple.report.application;

import jakarta.transaction.Transactional;
import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import kr.co.deepple.report.application.dto.ReportCreateRequest;
import kr.co.deepple.report.application.port.out.ReportRepository;
import kr.co.deepple.report.application.port.out.spi.ReportTargetFinder;
import kr.co.deepple.report.domain.ContentReportedEvent;
import kr.co.deepple.report.domain.Report;
import kr.co.deepple.report.domain.ReportErrorCode;
import kr.co.deepple.report.domain.ReportReasonType;
import kr.co.deepple.report.domain.ReportTargetType;
import kr.co.deepple.report.event.ReportCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

  private static final String DUPLICATE_REPORT_CONSTRAINT =
      "uk_reports_target_type_target_id_reporter_id";

  private final ReportRepository reportRepository;
  private final FindMemberSummaryUseCase findMemberSummaryUseCase;
  private final ReportTargetFinder reportTargetFinder;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void report(ReportCreateRequest request, long reporterId) {
    ReportTargetType targetType = ReportTargetType.from(request.targetType());
    ReportReasonType reason = ReportReasonType.from(request.reason());
    Report report =
        Report.of(
            reporterId,
            request.reporteeId(),
            targetType,
            request.targetId(),
            reason,
            request.content());
    validateNotReported(report);
    validateTarget(report);
    save(report);
    eventPublisher.publishEvent(ReportCreatedEvent.of(reporterId, request.reporteeId()));
    if (report.isContentReport()) {
      eventPublisher.publishEvent(new ContentReportedEvent(targetType, request.targetId()));
    }
  }

  private void validateTarget(Report report) {
    switch (report.getTargetType()) {
      case MEMBER -> findMemberSummaryUseCase.getMemberSummary(report.getReporteeId());
      case FEED ->
          validateAuthor(
              report,
              reportTargetFinder.findFeedAuthorMemberId(
                  report.getReporterId(), report.getTargetId()));
      case COMMENT ->
          validateAuthor(
              report,
              reportTargetFinder.findCommentAuthorMemberId(
                  report.getReporterId(), report.getTargetId()));
    }
  }

  private void validateAuthor(Report report, Optional<Long> authorMemberId) {
    if (authorMemberId.filter(id -> id == report.getReporteeId()).isEmpty()) {
      throw new DeeppleException(
          ReportErrorCode.INVALID_REPORT_TARGET,
          "targetType="
              + report.getTargetType()
              + ", targetId="
              + report.getTargetId()
              + ", reporteeId="
              + report.getReporteeId());
    }
  }

  private void validateNotReported(Report report) {
    if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
        report.getReporterId(), report.getTargetType(), report.getTargetId())) {
      throw reportAlreadyExistsException(report, null);
    }
  }

  private void save(Report report) {
    try {
      reportRepository.save(report);
    } catch (DataIntegrityViolationException e) {
      if (isDuplicateReport(e)) {
        throw reportAlreadyExistsException(report, e);
      }
      throw e;
    }
  }

  // MySQL은 위반한 제약조건 이름을 `reports.uk_...`처럼 테이블 이름을 붙여 알려준다.
  private static boolean isDuplicateReport(DataIntegrityViolationException e) {
    return e.getCause() instanceof ConstraintViolationException violation
        && violation.getConstraintName() != null
        && violation.getConstraintName().endsWith(DUPLICATE_REPORT_CONSTRAINT);
  }

  private DeeppleException reportAlreadyExistsException(
      Report report, DataIntegrityViolationException cause) {
    return new DeeppleException(
        ReportErrorCode.REPORT_ALREADY_EXISTS,
        "reporterId="
            + report.getReporterId()
            + ", targetType="
            + report.getTargetType()
            + ", targetId="
            + report.getTargetId(),
        cause);
  }
}
