package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record OfficialFeedGridItemView(
    long feedId,
    @Schema(description = "첫 번째 사진의 썸네일") String thumbnailUrl,
    String content,
    long likeCount,
    long commentCount) {}
