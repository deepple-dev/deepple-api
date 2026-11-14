package kr.co.deepple.report.application.port.out;

import java.util.Optional;
import kr.co.deepple.report.application.dto.RawReportDetailView;
import kr.co.deepple.report.application.dto.RawReportView;
import kr.co.deepple.report.application.dto.ReportSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportQueryRepository {
  Page<RawReportView> getPage(ReportSearchCondition condition, Pageable pageable);

  Optional<RawReportDetailView> findReportDetailView(long id);
}
