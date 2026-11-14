package kr.co.deepple.report.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.co.deepple.report.domain.ReportReasonType;

public record ReportCreateRequest(
    @NotNull(message = "신고 대상 아이디를 입력해주세요.") Long reporteeId,
    @NotBlank(message = "신고 사유를 입력해주세요.") @Schema(implementation = ReportReasonType.class)
        String reason,
    @NotNull(message = "신고 내용을 입력해주세요.") String content) {}
