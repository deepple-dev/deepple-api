package kr.co.deepple.report.application.port.out;

import kr.co.deepple.report.domain.Report;
import kr.co.deepple.report.domain.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

  boolean existsByReporterIdAndTargetTypeAndTargetId(
      long reporterId, ReportTargetType targetType, long targetId);

  long countByTargetTypeAndTargetId(ReportTargetType targetType, long targetId);
}
