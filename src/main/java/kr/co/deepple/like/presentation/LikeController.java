package kr.co.deepple.like.presentation;

import kr.co.deepple.auth.presentation.CurrentMemberId;
import kr.co.deepple.like.command.application.LikeSendService;
import kr.co.deepple.like.query.LikeQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "좋아요 관리 API")
@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {
    private final LikeSendService likeSendService;
    private final LikeQueryService likeQueryService;

    @Operation(summary = "좋아요 보내기")
    @PostMapping
    public ResponseEntity<LikeSendResponse> send(
        @Valid @RequestBody LikeSendRequest request,
        @CurrentMemberId long memberId
    ) {
        final long senderId = memberId;
        boolean hasProcessedMission = likeSendService.send(senderId, request);
        LikeSendResponse response = new LikeSendResponse(hasProcessedMission);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내가 보낸 좋아요 목록 조회")
    @GetMapping("/sent")
    public ResponseEntity<LikeViews> getSentLikes(
        @ModelAttribute LikeListRequest request,
        @CurrentMemberId long memberId
    ) {
        final var sentLikes = likeQueryService.findSentLikes(memberId, request.lastLikeId());
        return ResponseEntity.ok(sentLikes);
    }

    @Operation(summary = "내가 받은 좋아요 목록 조회")
    @GetMapping("/received")
    public ResponseEntity<LikeViews> getReceivedLikes(
        @ModelAttribute LikeListRequest request,
        @CurrentMemberId long memberId
    ) {
        final var receivedLikes = likeQueryService.findReceivedLikes(memberId, request.lastLikeId());
        return ResponseEntity.ok(receivedLikes);
    }
}
