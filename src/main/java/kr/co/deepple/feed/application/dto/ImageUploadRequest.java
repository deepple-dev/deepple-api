package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ImageUploadRequest(
    @Schema(description = "image/webp, image/jpeg, image/png 중 하나", example = "image/webp")
        @NotBlank(message = "이미지 형식은 필수입니다.")
        String contentType) {}
