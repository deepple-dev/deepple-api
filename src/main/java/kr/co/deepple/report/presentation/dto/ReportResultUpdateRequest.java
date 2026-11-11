package kr.co.deepple.report.presentation.dto;

import kr.co.deepple.report.command.domain.ReportResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportResultUpdateRequest(
    @NotNull(message = "버전을 입력해주세요.")
    Long version,
    @NotBlank(message = "신고 결과를 입력해주세요.")
    @Schema(implementation = ReportResult.class)
    String result
) {
}
