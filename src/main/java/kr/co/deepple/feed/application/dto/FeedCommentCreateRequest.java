package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FeedCommentCreateRequest(
    @NotBlank(message = "댓글 내용은 필수입니다.") @Size(max = 100, message = "댓글 내용은 100자 이하여야 합니다.")
        String content,
    @Schema(description = "답글이면 부모 댓글 ID. 답글에는 답글을 달 수 없다") Long parentCommentId) {}
