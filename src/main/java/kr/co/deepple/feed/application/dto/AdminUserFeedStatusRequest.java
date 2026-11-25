package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import kr.co.deepple.feed.domain.FeedStatus;

public record AdminUserFeedStatusRequest(
    @Schema(implementation = FeedStatus.class, description = "PUBLISHED(공개)·REJECTED(반려) 중 하나")
        @NotBlank(message = "상태는 필수입니다.")
        String status) {}
