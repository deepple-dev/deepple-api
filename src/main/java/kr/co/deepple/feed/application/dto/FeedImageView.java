package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FeedImageView(
    long imageId, String imageUrl, @Schema(description = "격자형 목록용 썸네일") String thumbnailUrl) {}
