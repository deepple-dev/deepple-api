package kr.co.deepple.like.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.like.application.LikeQueryService;
import kr.co.deepple.like.application.LikeSendService;
import kr.co.deepple.like.application.dto.LikeListRequest;
import kr.co.deepple.like.application.dto.LikeSendRequest;
import kr.co.deepple.like.application.dto.LikeSendResponse;
import kr.co.deepple.like.application.dto.LikeViews;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.LIKE)
@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {

  private final LikeSendService likeSendService;
  private final LikeQueryService likeQueryService;

  @Operation(summary = "좋아요 보내기")
  @PostMapping
  public ResponseEntity<LikeSendResponse> send(
      @Valid @RequestBody LikeSendRequest request, @CurrentMemberId long memberId) {
    long senderId = memberId;
    boolean hasProcessedMission = likeSendService.send(senderId, request);
    LikeSendResponse response = new LikeSendResponse(hasProcessedMission);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "내가 보낸 좋아요 목록 조회")
  @GetMapping("/sent")
  public ResponseEntity<LikeViews> getSentLikes(
      @ModelAttribute LikeListRequest request, @CurrentMemberId long memberId) {
    LikeViews sentLikes = likeQueryService.findSentLikes(memberId, request.lastLikeId());
    return ResponseEntity.ok(sentLikes);
  }

  @Operation(summary = "내가 받은 좋아요 목록 조회")
  @GetMapping("/received")
  public ResponseEntity<LikeViews> getReceivedLikes(
      @ModelAttribute LikeListRequest request, @CurrentMemberId long memberId) {
    LikeViews receivedLikes = likeQueryService.findReceivedLikes(memberId, request.lastLikeId());
    return ResponseEntity.ok(receivedLikes);
  }
}
