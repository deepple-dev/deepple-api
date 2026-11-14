package kr.co.deepple.feed.application.dto;

import jakarta.validation.constraints.NotNull;

public record FeedVoteRequest(@NotNull(message = "선택지 ID는 필수입니다.") Long optionId) {}
