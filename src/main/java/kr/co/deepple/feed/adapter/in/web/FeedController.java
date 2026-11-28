package kr.co.deepple.feed.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.feed.application.dto.FeedBackgroundViews;
import kr.co.deepple.feed.application.dto.FeedCreateRequest;
import kr.co.deepple.feed.application.dto.FeedGridViews;
import kr.co.deepple.feed.application.dto.FeedPollView;
import kr.co.deepple.feed.application.dto.FeedSearchCondition;
import kr.co.deepple.feed.application.dto.FeedView;
import kr.co.deepple.feed.application.dto.FeedViews;
import kr.co.deepple.feed.application.dto.FeedVoteRequest;
import kr.co.deepple.feed.application.dto.OfficialFeedGridViews;
import kr.co.deepple.feed.application.port.in.CreateFeedUseCase;
import kr.co.deepple.feed.application.port.in.DeleteFeedUseCase;
import kr.co.deepple.feed.application.port.in.FindHomeFeedsUseCase;
import kr.co.deepple.feed.application.port.in.FindMyFeedsUseCase;
import kr.co.deepple.feed.application.port.in.FindOfficialFeedsUseCase;
import kr.co.deepple.feed.application.port.in.GetFeedBackgroundsUseCase;
import kr.co.deepple.feed.application.port.in.GetFeedUseCase;
import kr.co.deepple.feed.application.port.in.LikeFeedUseCase;
import kr.co.deepple.feed.application.port.in.UnlikeFeedUseCase;
import kr.co.deepple.feed.application.port.in.VoteFeedUseCase;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class FeedController {

  private final GetFeedBackgroundsUseCase getFeedBackgroundsUseCase;
  private final CreateFeedUseCase createFeedUseCase;
  private final DeleteFeedUseCase deleteFeedUseCase;
  private final GetFeedUseCase getFeedUseCase;
  private final FindMyFeedsUseCase findMyFeedsUseCase;
  private final FindHomeFeedsUseCase findHomeFeedsUseCase;
  private final FindOfficialFeedsUseCase findOfficialFeedsUseCase;
  private final LikeFeedUseCase likeFeedUseCase;
  private final UnlikeFeedUseCase unlikeFeedUseCase;
  private final VoteFeedUseCase voteFeedUseCase;

  @Operation(
      summary = "피드 목록 조회",
      description =
          "홈 피드를 최대 20개씩 조회한다. 차단·비활성 작성자의 피드를 빼서 20개보다 적거나 0개일 수 있으니, 다음 페이지가 있는지는 hasMore로 판단한다. 상단 고정 피드가 먼저 온다.")
  @GetMapping("/feeds")
  public ResponseEntity<FeedViews> getFeeds(
      @CurrentMemberId long memberId,
      @Valid @ModelAttribute FeedSearchCondition feedSearchCondition) {
    return ResponseEntity.ok(
        findHomeFeedsUseCase.findHomeFeeds(memberId, feedSearchCondition.cursor()));
  }

  @Operation(
      summary = "내가 쓴 글 목록 조회",
      description =
          "격자형 목록. 최신순이고 심사 대기·반려된 피드도 포함한다. 칸을 누르면 피드 상세를 조회한다. totalCount는 같은 조건의 전체 개수다.")
  @GetMapping("/me/feeds")
  public ResponseEntity<FeedGridViews> getMyFeeds(
      @CurrentMemberId long memberId,
      @Parameter(description = "이전 응답의 nextCursor. 첫 페이지는 비운다") @RequestParam(required = false)
          String cursor) {
    return ResponseEntity.ok(findMyFeedsUseCase.findMyFeeds(memberId, cursor));
  }

  @Operation(
      summary = "공식 계정 피드 목록 조회",
      description =
          "공식 계정 화면의 격자형 목록. 상단 고정 피드가 먼저 오고 나머지는 최신순이다. 칸을 누르면 피드 상세를 조회한다. totalCount는 같은 조건의 전체 개수다.")
  @GetMapping("/official/feeds")
  public ResponseEntity<OfficialFeedGridViews> getOfficialFeeds(
      @CurrentMemberId long memberId,
      @Parameter(description = "이전 응답의 nextCursor. 첫 페이지는 비운다") @RequestParam(required = false)
          String cursor) {
    return ResponseEntity.ok(findOfficialFeedsUseCase.findOfficialFeeds(memberId, cursor));
  }

  @Operation(
      summary = "내가 쓴 글 하나씩 보기 목록 조회",
      description =
          "내 피드 격자(/me/feeds)와 같은 순서·같은 커서로, 피드 상세와 같은 모양을 최대 20개씩 준다. 심사 대기·반려된 피드도 포함한다. 격자에서 누른 칸이 있는 페이지의 커서로 부르고, 누른 피드는 feedId로 찾는다.")
  @GetMapping("/me/feeds/views")
  public ResponseEntity<FeedViews> getMyFeedViews(
      @CurrentMemberId long memberId,
      @Parameter(description = "격자와 같은 커서. 첫 페이지는 비운다") @RequestParam(required = false)
          String cursor) {
    return ResponseEntity.ok(findMyFeedsUseCase.findMyFeedViews(memberId, cursor));
  }

  @Operation(
      summary = "공식 계정 피드 하나씩 보기 목록 조회",
      description =
          "공식 계정 격자(/official/feeds)와 같은 순서·같은 커서로, 피드 상세와 같은 모양을 최대 20개씩 준다. 격자에서 누른 칸이 있는 페이지의 커서로 부르고, 누른 피드는 feedId로 찾는다.")
  @GetMapping("/official/feeds/views")
  public ResponseEntity<FeedViews> getOfficialFeedViews(
      @CurrentMemberId long memberId,
      @Parameter(description = "격자와 같은 커서. 첫 페이지는 비운다") @RequestParam(required = false)
          String cursor) {
    return ResponseEntity.ok(findOfficialFeedsUseCase.findOfficialFeedViews(memberId, cursor));
  }

  @Operation(summary = "피드 상세 조회")
  @GetMapping("/feeds/{feedId}")
  public ResponseEntity<FeedView> getFeed(
      @CurrentMemberId long memberId, @PathVariable long feedId) {
    return ResponseEntity.ok(getFeedUseCase.getFeed(memberId, feedId));
  }

  @Operation(
      summary = "피드 등록",
      description = "사진이 없으면 바로 공개되고, 사진이 있으면 심사를 거친다. 같은 Idempotency-Key로 다시 보내면 처음 만든 피드를 돌려준다.")
  @PostMapping("/feeds")
  public ResponseEntity<FeedView> createFeed(
      @CurrentMemberId long memberId,
      @Parameter(description = "등록마다 새로 만든 UUID. 재전송할 때는 같은 키를 쓴다")
          @RequestHeader("Idempotency-Key")
          @NotBlank
          @Size(max = 100)
          String idempotencyKey,
      @Valid @RequestBody FeedCreateRequest request) {
    return ResponseEntity.ok(createFeedUseCase.createFeed(memberId, idempotencyKey, request));
  }

  @Operation(summary = "피드 삭제", description = "본인 피드만 삭제할 수 있다. 댓글도 함께 삭제된다.")
  @DeleteMapping("/feeds/{feedId}")
  public ResponseEntity<Void> deleteFeed(
      @CurrentMemberId long memberId, @PathVariable long feedId) {
    deleteFeedUseCase.deleteFeed(memberId, feedId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "피드 좋아요", description = "이미 누른 상태여도 같은 응답을 준다.")
  @PutMapping("/feeds/{feedId}/like")
  public ResponseEntity<Void> likeFeed(@CurrentMemberId long memberId, @PathVariable long feedId) {
    likeFeedUseCase.likeFeed(memberId, feedId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "피드 좋아요 취소", description = "누르지 않은 상태여도 같은 응답을 준다.")
  @DeleteMapping("/feeds/{feedId}/like")
  public ResponseEntity<Void> unlikeFeed(
      @CurrentMemberId long memberId, @PathVariable long feedId) {
    unlikeFeedUseCase.unlikeFeed(memberId, feedId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "투표하기", description = "한 번만 투표할 수 있고 바꿀 수 없다. 응답에 결과와 내 선택이 들어 있다.")
  @PostMapping("/feeds/{feedId}/votes")
  public ResponseEntity<FeedPollView> vote(
      @CurrentMemberId long memberId,
      @PathVariable long feedId,
      @Valid @RequestBody FeedVoteRequest request) {
    return ResponseEntity.ok(voteFeedUseCase.vote(memberId, feedId, request.optionId()));
  }

  @Operation(summary = "피드 배경 목록 조회")
  @GetMapping("/feed-backgrounds")
  public ResponseEntity<FeedBackgroundViews> getBackgrounds() {
    return ResponseEntity.ok(getFeedBackgroundsUseCase.getFeedBackgrounds());
  }
}
