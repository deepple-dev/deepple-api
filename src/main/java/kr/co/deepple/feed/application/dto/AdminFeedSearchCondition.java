package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.feed.domain.FeedAuthorType;
import kr.co.deepple.feed.domain.FeedStatus;

public record AdminFeedSearchCondition(
    @Schema(implementation = FeedStatus.class, description = "PENDING이면 사진 심사 목록") String status,
    @Schema(implementation = FeedAuthorType.class, description = "OFFICIAL이면 관리자 피드 목록")
        String authorType) {}
