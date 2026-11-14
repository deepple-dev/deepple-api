package kr.co.deepple.report.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import kr.co.deepple.report.domain.ReportResult;

public record ReportSearchCondition(
    @Schema(implementation = ReportResult.class) String result,
    LocalDate createdAtGoe,
    LocalDate createdAtLoe) {}
