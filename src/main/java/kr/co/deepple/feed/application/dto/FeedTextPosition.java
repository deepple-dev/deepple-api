package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Schema(description = "글 위치. 캔버스(4:5) 대비 텍스트 박스 중심의 비율 좌표")
public record FeedTextPosition(
    @Schema(description = "가로 위치(0~1)", example = "0.5")
        @NotNull(message = "글 가로 위치는 필수입니다.")
        @DecimalMin(value = "0", message = "글 가로 위치는 0 이상이어야 합니다.")
        @DecimalMax(value = "1", message = "글 가로 위치는 1 이하여야 합니다.")
        Double x,
    @Schema(description = "세로 위치(0~1)", example = "0.4")
        @NotNull(message = "글 세로 위치는 필수입니다.")
        @DecimalMin(value = "0", message = "글 세로 위치는 0 이상이어야 합니다.")
        @DecimalMax(value = "1", message = "글 세로 위치는 1 이하여야 합니다.")
        Double y,
    @Schema(description = "회전 각도. 지금은 0으로 고정", example = "0") @NotNull(message = "글 회전 각도는 필수입니다.")
        Double rotation,
    @Schema(description = "배율. 지금은 1로 고정", example = "1") @NotNull(message = "글 배율은 필수입니다.")
        Double scale) {}
