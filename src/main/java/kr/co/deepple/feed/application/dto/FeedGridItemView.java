package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.feed.domain.FeedType;

public record FeedGridItemView(
    long feedId,
    @Schema(implementation = FeedType.class) String type,
    @Schema(description = "사진 또는 배경의 썸네일") String thumbnailUrl,
    @Schema(description = "글 피드는 내용, 투표 피드는 질문") String content,
    FeedTextPosition textPosition) {}
