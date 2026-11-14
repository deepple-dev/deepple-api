package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FeedPollOptionView(
    long optionId,
    String content,
    @Schema(description = "득표율(소수점 없는 퍼센트). 결과를 볼 수 없으면 null", example = "67")
        Integer votePercent) {}
