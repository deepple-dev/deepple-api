package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import kr.co.deepple.feed.domain.FeedType;

public record FeedView(
    long feedId,
    @Schema(implementation = FeedType.class) String type,
    FeedAuthorView author,
    @Schema(description = "글 피드·관리자 피드는 내용, 투표 피드는 질문") String content,
    @Schema(description = "글 위치. 관리자 피드는 null") FeedTextPosition textPosition,
    @Schema(description = "배경. 사진을 올린 피드와 관리자 피드는 null") FeedBackgroundView background,
    @Schema(description = "사진. 사용자 피드는 최대 1장, 관리자 피드는 최대 5장") List<FeedImageView> images,
    @Schema(description = "투표. 투표 피드가 아니면 null") FeedPollView poll,
    @Schema(description = "상단 고정 여부") boolean isPinned,
    long likeCount,
    @Schema(description = "내가 좋아요를 눌렀는지") boolean isLiked,
    long commentCount,
    @Schema(description = "내가 쓴 피드인지") boolean isMine,
    LocalDateTime createdAt) {}
