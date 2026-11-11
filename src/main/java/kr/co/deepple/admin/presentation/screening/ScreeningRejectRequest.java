package kr.co.deepple.admin.presentation.screening;

import kr.co.deepple.admin.command.domain.screening.RejectionReasonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ScreeningRejectRequest(
    @Schema(
        description = "반려 사유",
        implementation = RejectionReasonType.class,
        example = "STOLEN_IMAGE"
    )
    @NotBlank(message = "반려 사유를 선택해주세요.")
    String rejectionReason,

    long version
) {
}
