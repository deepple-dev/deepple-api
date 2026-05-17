package deepple.deepple.heart.presentation.hearttransaction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AdminHeartGrantRequest(
    @Schema(description = "지급 대상 멤버 ID", example = "1")
    @NotNull Long memberId,

    @Schema(description = "지급 하트 수량 (양수)", example = "10")
    @NotNull @Positive Long amount,

    @Schema(description = "지급 사유 (필수)", example = "CS 보상")
    @NotBlank @Size(max = 100) String reason
) {
}
