package kr.co.deepple.report.command.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportCommandRepository extends JpaRepository<Report, Long> {
    boolean existsByReporterIdAndReporteeId(long reporterId, long reporteeId);
}
