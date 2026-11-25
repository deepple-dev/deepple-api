package kr.co.deepple.report.domain;

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
import kr.co.deepple.common.exception.DeeppleException;
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

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)", nullable = false)
  private ReportTargetType targetType;

  private long targetId;

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
      @NonNull ReportTargetType targetType,
      long targetId,
      Long adminId,
      @NonNull ReportReasonType reason,
      @NonNull String content,
      @NonNull ReportResult result) {
    if (reporterId == reporteeId) {
      throw new DeeppleException(ReportErrorCode.SELF_REPORT, "memberId=" + reporterId);
    }
    if (targetType == ReportTargetType.MEMBER && targetId != reporteeId) {
      throw new DeeppleException(
          ReportErrorCode.INVALID_REPORT_TARGET,
          "reporteeId=" + reporteeId + ", targetId=" + targetId);
    }
    this.reporterId = reporterId;
    this.reporteeId = reporteeId;
    this.targetType = targetType;
    this.targetId = targetId;
    this.adminId = adminId;
    this.reason = reason;
    this.content = content;
    this.result = result;
  }

  public static Report of(
      long reporterId,
      long reporteeId,
      ReportTargetType targetType,
      long targetId,
      ReportReasonType reason,
      String content) {
    return new Report(
        reporterId, reporteeId, targetType, targetId, null, reason, content, ReportResult.PENDING);
  }

  public boolean isContentReport() {
    return targetType != ReportTargetType.MEMBER;
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
