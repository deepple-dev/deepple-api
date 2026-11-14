package kr.co.deepple.feed.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.feed.application.dto.FeedBackgroundViews;
import kr.co.deepple.feed.application.dto.FeedCreateRequest;
import kr.co.deepple.feed.application.dto.FeedGridViews;
import kr.co.deepple.feed.application.dto.FeedPollView;
import kr.co.deepple.feed.application.dto.FeedSearchCondition;
import kr.co.deepple.feed.application.dto.FeedView;
import kr.co.deepple.feed.application.dto.FeedViews;
import kr.co.deepple.feed.application.dto.FeedVoteRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.FEED)
@RestController
public class FeedController {

  @Operation(
      summary = "피드 목록 조회",
      description = "홈 피드를 20개씩 조회한다. authorType=OFFICIAL이면 관리자 피드 모아보기다.")
  @GetMapping("/feeds")
  public ResponseEntity<FeedViews> getFeeds(
      @Valid @ModelAttribute FeedSearchCondition feedSearchCondition) {
    return ResponseEntity.ok(FeedSamples.feeds(feedSearchCondition));
  }

  @Operation(
      summary = "내가 쓴 글 목록 조회",
      description = "격자형 목록. 심사 대기·반려된 피드도 포함한다. 칸을 누르면 피드 상세를 조회한다.")
  @GetMapping("/me/feeds")
  public ResponseEntity<FeedGridViews> getMyFeeds(
      @Parameter(description = "이전 응답의 nextCursor. 첫 페이지는 비운다") @RequestParam(required = false)
          String cursor) {
    return ResponseEntity.ok(FeedSamples.myFeedGrid());
  }

  @Operation(summary = "피드 상세 조회")
  @GetMapping("/feeds/{feedId}")
  public ResponseEntity<FeedView> getFeed(@PathVariable long feedId) {
    return ResponseEntity.ok(FeedSamples.feed(feedId));
  }

  @Operation(
      summary = "피드 등록",
      description = "사진이 없으면 바로 공개되고, 사진이 있으면 심사를 거친다. 같은 Idempotency-Key로 다시 보내면 처음 결과를 돌려준다.")
  @PostMapping("/feeds")
  public ResponseEntity<FeedView> createFeed(
      @Parameter(description = "등록마다 새로 만든 고유한 키. 재전송할 때는 같은 키를 쓴다")
          @RequestHeader("Idempotency-Key")
          String idempotencyKey,
      @Valid @RequestBody FeedCreateRequest request) {
    return ResponseEntity.ok(FeedSamples.createdFeed());
  }

  @Operation(summary = "피드 삭제", description = "본인 피드만 삭제할 수 있다. 댓글도 함께 삭제된다.")
  @DeleteMapping("/feeds/{feedId}")
  public ResponseEntity<Void> deleteFeed(@PathVariable long feedId) {
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "피드 좋아요", description = "이미 누른 상태여도 같은 응답을 준다.")
  @PutMapping("/feeds/{feedId}/like")
  public ResponseEntity<Void> likeFeed(@PathVariable long feedId) {
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "피드 좋아요 취소", description = "누르지 않은 상태여도 같은 응답을 준다.")
  @DeleteMapping("/feeds/{feedId}/like")
  public ResponseEntity<Void> unlikeFeed(@PathVariable long feedId) {
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "투표하기", description = "한 번만 투표할 수 있고 바꿀 수 없다. 응답에 결과와 내 선택이 들어 있다.")
  @PostMapping("/feeds/{feedId}/votes")
  public ResponseEntity<FeedPollView> vote(
      @PathVariable long feedId, @Valid @RequestBody FeedVoteRequest request) {
    return ResponseEntity.ok(FeedSamples.vote(feedId, request.optionId()));
  }

  @Operation(summary = "피드 배경 목록 조회")
  @GetMapping("/feed-backgrounds")
  public ResponseEntity<FeedBackgroundViews> getBackgrounds() {
    return ResponseEntity.ok(FeedSamples.backgrounds());
  }
}
