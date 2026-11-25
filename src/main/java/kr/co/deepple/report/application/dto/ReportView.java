package kr.co.deepple.report.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.co.deepple.report.domain.ReportReasonType;
import kr.co.deepple.report.domain.ReportResult;
import kr.co.deepple.report.domain.ReportTargetType;

public record ReportView(
    Long id,
    Long reporterId,
    String reporterNickname,
    Long reporteeId,
    String reporteeNickname,
    @Schema(implementation = ReportTargetType.class) String targetType,
    @Schema(description = "신고 대상 ID. 회원 신고면 reporteeId와 같다") Long targetId,
    @Schema(implementation = ReportReasonType.class) String reason,
    @Schema(implementation = ReportResult.class) String result,
    LocalDateTime createdAt) {}
