package kr.co.deepple.report.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;

public record RawReportView(
    Long id,
    Long reporterId,
    Long reporteeId,
    String reason,
    String result,
    LocalDateTime createdAt) {

  @QueryProjection
  public RawReportView {}
}
