package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record FeedCommentView(
    long commentId,
    @Schema(description = "부모 댓글 ID. 최상위 댓글이면 null") Long parentCommentId,
    @Schema(
            implementation = FeedCommentDisplayStatus.class,
            description =
                "VISIBLE: 정상, DELETED: 삭제된 댓글입니다, HIDDEN: 숨겨진 댓글입니다, "
                    + "INACTIVE_AUTHOR: 비활성화 회원이 작성한 글입니다")
        String displayStatus,
    @Schema(description = "작성자. VISIBLE이 아니면 null") FeedAuthorView author,
    @Schema(description = "내용. VISIBLE이 아니면 null") String content,
    @Schema(description = "답글 수. 답글이면 0") int replyCount,
    @Schema(description = "내가 쓴 댓글인지") boolean isMine,
    LocalDateTime createdAt) {}
