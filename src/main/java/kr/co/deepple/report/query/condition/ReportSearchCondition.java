package kr.co.deepple.report.query.condition;

import kr.co.deepple.report.command.domain.ReportResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record ReportSearchCondition(
    @Schema(implementation = ReportResult.class)
    String result,
    LocalDate createdAtGoe,
    LocalDate createdAtLoe
) {
}
