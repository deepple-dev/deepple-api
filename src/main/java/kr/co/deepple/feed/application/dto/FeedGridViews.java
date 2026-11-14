package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record FeedGridViews(
    List<FeedGridItemView> feeds,
    @Schema(description = "다음 페이지 요청에 넣을 커서. 마지막 페이지면 null") String nextCursor,
    boolean hasMore) {}
