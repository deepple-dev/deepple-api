package deepple.deepple.report.command.domain;

import deepple.deepple.common.entity.BaseEntity;
import deepple.deepple.common.event.Events;
import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.report.command.domain.event.ReportCreatedEvent;
import deepple.deepple.report.command.domain.event.ReportWarnedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "reports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    private Long reporterId;

    @Getter
    private Long reporteeId;

    @Getter
    private Long adminId;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private ReportReasonType reason;

    @Getter
    private String content;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private ReportResult result;

    @Version
    @Getter
    private Long version;

    private Report(long reporterId, long reporteeId, Long adminId, ReportReasonType reason, String content,
        ReportResult status) {
        validateReport(reporterId, reporteeId);
        setReporterId(reporterId);
        setReporteeId(reporteeId);
        setAdminId(adminId);
        setReason(reason);
        setContent(content);
        setResult(status);
    }

    public static Report of(long reporterId, long reporteeId, ReportReasonType reason, String content) {
        Report report = new Report(reporterId, reporteeId, null, reason, content, ReportResult.PENDING);
        Events.raise(ReportCreatedEvent.of(reporterId, reporteeId));
        return report;
    }

    public void reject(long adminId) {
        validateResult();
        setAdminId(adminId);
        setResult(ReportResult.REJECTED);
    }

    public void warn(long adminId) {
        validateResult();
        setAdminId(adminId);
        setResult(ReportResult.WARNED);
        Events.raise(ReportWarnedEvent.of(adminId, reporteeId, reason.name()));
    }

    public boolean hasVersionConflict(long version) {
        return this.version != version;
    }

    private void validateReport(long reporterId, long reporteeId) {
        if (reporterId == reporteeId) {
            throw new DeeppleException(ReportErrorCode.SELF_REPORT, "memberId=" + reporterId);
        }
    }

    private void validateResult() {
        if (this.result != ReportResult.PENDING) {
            throw new DeeppleException(
                ReportErrorCode.REPORT_ALREADY_PROCESSED,
                "reportId=" + id + ", result=" + result
            );
        }
    }

    private void setReporterId(long reporterId) {
        this.reporterId = reporterId;
    }

    private void setReporteeId(long reporteeId) {
        this.reporteeId = reporteeId;
    }

    private void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    private void setReason(@NonNull ReportReasonType reason) {
        this.reason = reason;
    }

    private void setContent(@NonNull String content) {
        this.content = content;
    }

    private void setResult(@NonNull ReportResult result) {
        this.result = result;
    }
}
