package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FeedSearchCondition(
    @Schema(description = "이전 응답의 nextCursor. 첫 페이지는 비운다") String cursor) {}
