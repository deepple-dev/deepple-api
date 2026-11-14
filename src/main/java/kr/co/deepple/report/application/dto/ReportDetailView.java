package kr.co.deepple.report.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.co.deepple.report.domain.ReportReasonType;
import kr.co.deepple.report.domain.ReportResult;

public record ReportDetailView(
    Long id,
    Long version,
    Long reporterId,
    String reporterNickname,
    Long reporteeId,
    String reporteeNickname,
    @Schema(implementation = ReportReasonType.class) String reason,
    @Schema(implementation = ReportResult.class) String result,
    String content,
    LocalDateTime createdAt) {
  @QueryProjection
  public ReportDetailView {}
}
