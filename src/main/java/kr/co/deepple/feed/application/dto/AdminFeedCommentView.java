package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.co.deepple.feed.domain.FeedCommentStatus;

public record AdminFeedCommentView(
    long commentId,
    long feedId,
    @Schema(description = "답글이면 부모 댓글 ID, 최상위 댓글이면 null") Long parentCommentId,
    @Schema(implementation = FeedCommentStatus.class) String status,
    long authorMemberId,
    String authorNickname,
    String content,
    LocalDateTime createdAt,
    @Schema(description = "지운 시각. 지우지 않았으면 null") LocalDateTime deletedAt) {}
