package kr.co.deepple.image.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ImageUploadResponse(
    @Schema(description = "피드 등록 등에 넣을 이미지 ID") long imageId,
    @Schema(description = "파일을 PUT으로 보낼 presigned URL") String uploadUrl) {}
