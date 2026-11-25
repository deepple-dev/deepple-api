package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.feed.domain.FeedStatus;

public record AdminUserFeedSearchCondition(
    @Schema(
            implementation = FeedStatus.class,
            description = "PENDING·PUBLISHED·REJECTED·HIDDEN_BY_REPORT 중 하나. 보내지 않으면 모든 상태")
        String status) {}
