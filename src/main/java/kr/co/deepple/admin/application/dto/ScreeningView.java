package kr.co.deepple.admin.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.admin.domain.RejectionReasonType;
import kr.co.deepple.admin.domain.ScreeningStatus;

public record ScreeningView(
    long screeningId,
    String nickname,
    @Schema(description = "성별", example = "MALE") String gender,
    String joinedDate,
    @Schema(implementation = ScreeningStatus.class) String screeningStatus,
    @Schema(implementation = RejectionReasonType.class) String rejectionReason) {}
