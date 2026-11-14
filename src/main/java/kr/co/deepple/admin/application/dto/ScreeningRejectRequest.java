package kr.co.deepple.admin.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import kr.co.deepple.admin.domain.RejectionReasonType;

public record ScreeningRejectRequest(
    @Schema(
            description = "반려 사유",
            implementation = RejectionReasonType.class,
            example = "STOLEN_IMAGE")
        @NotBlank(message = "반려 사유를 선택해주세요.")
        String rejectionReason,
    long version) {}
