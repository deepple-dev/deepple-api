package kr.co.deepple.report.fixture;

import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import kr.co.deepple.report.domain.Report;
import kr.co.deepple.report.domain.ReportReasonType;
import kr.co.deepple.report.domain.ReportResult;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportFixture {

  private Long id;
  private long reporterId = 1L;
  private long reporteeId = 2L;
  private Long adminId;
  private ReportReasonType reason = ReportReasonType.STOLEN_IMAGE;
  private String content = "신고 내용";
  private ReportResult result = ReportResult.PENDING;
  private Long version;

  public static ReportFixture builder() {
    return new ReportFixture();
  }

  public Report build() {
    Report report = instantiateClass(Report.class);
    setField(report, "id", id);
    setField(report, "reporterId", reporterId);
    setField(report, "reporteeId", reporteeId);
    setField(report, "adminId", adminId);
    setField(report, "reason", reason);
    setField(report, "content", content);
    setField(report, "result", result);
    setField(report, "version", version);
    return report;
  }
}
