package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.feed.domain.FeedAuthorType;

public record FeedSearchCondition(
    @Schema(implementation = FeedAuthorType.class, description = "OFFICIAL이면 관리자 피드 모아보기. 없으면 홈 피드")
        String authorType,
    @Schema(description = "이전 응답의 nextCursor. 첫 페이지는 비운다") String cursor) {}
