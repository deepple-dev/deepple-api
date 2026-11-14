package kr.co.deepple.feed.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.feed.application.dto.FeedCommentCreateRequest;
import kr.co.deepple.feed.application.dto.FeedCommentView;
import kr.co.deepple.feed.application.dto.FeedCommentViews;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.FEED)
@RestController
public class FeedCommentController {

  @Operation(summary = "댓글 목록 조회", description = "최상위 댓글을 오래된 순으로 20개씩 조회한다. 댓글마다 답글 수가 들어 있다.")
  @GetMapping("/feeds/{feedId}/comments")
  public ResponseEntity<FeedCommentViews> getComments(
      @PathVariable long feedId,
      @Parameter(description = "이전 응답의 nextCursor. 첫 페이지는 비운다") @RequestParam(required = false)
          String cursor) {
    return ResponseEntity.ok(FeedSamples.comments(cursor));
  }

  @Operation(summary = "댓글 작성", description = "답글이면 부모 댓글 ID를 함께 보낸다.")
  @PostMapping("/feeds/{feedId}/comments")
  public ResponseEntity<FeedCommentView> createComment(
      @PathVariable long feedId, @Valid @RequestBody FeedCommentCreateRequest request) {
    return ResponseEntity.ok(FeedSamples.createdComment(request.parentCommentId()));
  }

  @Operation(summary = "답글 목록 조회", description = "댓글의 답글을 오래된 순으로 20개씩 조회한다.")
  @GetMapping("/comments/{commentId}/replies")
  public ResponseEntity<FeedCommentViews> getReplies(
      @PathVariable long commentId,
      @Parameter(description = "이전 응답의 nextCursor. 첫 페이지는 비운다") @RequestParam(required = false)
          String cursor) {
    return ResponseEntity.ok(FeedSamples.replies(commentId));
  }

  @Operation(summary = "댓글 삭제", description = "본인 또는 피드 작성자가 삭제할 수 있다.")
  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<Void> deleteComment(@PathVariable long commentId) {
    return ResponseEntity.noContent().build();
  }
}
