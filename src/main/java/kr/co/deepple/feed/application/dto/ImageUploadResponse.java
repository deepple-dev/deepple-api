package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

public record ImageUploadResponse(
    @Schema(description = "피드 등록 등에 넣을 이미지 ID") long imageId,
    @Schema(description = "presigned POST를 보낼 URL") String uploadUrl,
    @Schema(description = "multipart/form-data에 그대로 넣을 필드. file 필드는 맨 마지막에 넣는다")
        Map<String, String> formFields) {}
