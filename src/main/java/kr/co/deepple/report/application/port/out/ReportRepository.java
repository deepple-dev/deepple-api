package kr.co.deepple.report.application.port.out;

import kr.co.deepple.report.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
  boolean existsByReporterIdAndReporteeId(long reporterId, long reporteeId);
}
