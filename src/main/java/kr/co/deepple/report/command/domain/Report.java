package kr.co.deepple.report.command.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import kr.co.deepple.common.entity.BaseEntity;
import kr.co.deepple.common.event.Events;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.report.command.domain.event.ReportCreatedEvent;
import kr.co.deepple.report.command.domain.event.ReportWarnedEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long reporterId;

  private long reporteeId;

  private Long adminId;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private ReportReasonType reason;

  private String content;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private ReportResult result;

  @Version private Long version;

  private Report(
      long reporterId,
      long reporteeId,
      Long adminId,
      @NonNull ReportReasonType reason,
      @NonNull String content,
      @NonNull ReportResult result) {
    if (reporterId == reporteeId) {
      throw new DeeppleException(ReportErrorCode.SELF_REPORT, "memberId=" + reporterId);
    }
    this.reporterId = reporterId;
    this.reporteeId = reporteeId;
    this.adminId = adminId;
    this.reason = reason;
    this.content = content;
    this.result = result;
  }

  public static Report of(
      long reporterId, long reporteeId, ReportReasonType reason, String content) {
    Report report = new Report(reporterId, reporteeId, null, reason, content, ReportResult.PENDING);
    Events.raise(ReportCreatedEvent.of(reporterId, reporteeId));
    return report;
  }

  public void reject(long adminId) {
    validateResult();
    this.adminId = adminId;
    this.result = ReportResult.REJECTED;
  }

  public void warn(long adminId) {
    validateResult();
    this.adminId = adminId;
    this.result = ReportResult.WARNED;
    Events.raise(ReportWarnedEvent.of(adminId, reporteeId, reason.name()));
  }

  public boolean hasVersionConflict(long version) {
    return this.version != version;
  }

  private void validateResult() {
    if (result != ReportResult.PENDING) {
      throw new DeeppleException(
          ReportErrorCode.REPORT_ALREADY_PROCESSED, "reportId=" + id + ", result=" + result);
    }
  }
}
