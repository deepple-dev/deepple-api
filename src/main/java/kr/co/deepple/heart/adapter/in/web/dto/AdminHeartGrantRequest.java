package kr.co.deepple.heart.adapter.in.web.dto;

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
    @NotBlank @Size(max = 100) String reason,
    @Schema(description = "같은 지급이 두 번 처리되지 않게 하는 키. 요청마다 새로 만들고 재시도할 때는 같은 값을 보낸다",
        example = "0b7c2c1e-6f0a-4c8f-9a1b-3c5d7e9f1a2b")
    @Size(max = 100) String idempotencyKey
) {
}
