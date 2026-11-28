package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record FeedGridViews(
    List<FeedGridItemView> feeds,
    @Schema(description = "다음 페이지 요청에 넣을 커서. 마지막 페이지면 null") String nextCursor,
    boolean hasMore,
    @Schema(description = "내가 쓴 글 전체 개수(지운 글만 빼고 모든 상태 포함). 페이지와 상관없이 같은 값") long totalCount) {}
