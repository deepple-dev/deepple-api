package kr.co.deepple.image.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ImageUploadRequest(
    @Schema(description = "image/webp, image/jpeg, image/png 중 하나", example = "image/webp")
        @NotBlank(message = "이미지 형식은 필수입니다.")
        String contentType,
    @Schema(description = "올릴 파일의 바이트 크기. 최대 10MB(10485760)", example = "524288")
        @NotNull(message = "파일 크기는 필수입니다.")
        @Positive(message = "파일 크기는 1바이트 이상이어야 합니다.")
        @Max(value = 10_485_760L, message = "파일 크기는 10MB 이하여야 합니다.")
        Long contentLength) {}
