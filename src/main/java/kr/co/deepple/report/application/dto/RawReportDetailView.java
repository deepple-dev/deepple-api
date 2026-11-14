package kr.co.deepple.report.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;

public record RawReportDetailView(
    Long id,
    Long version,
    Long reporterId,
    Long reporteeId,
    String reason,
    String result,
    String content,
    LocalDateTime createdAt) {

  @QueryProjection
  public RawReportDetailView {}
}
