package kr.co.deepple.match.presentation;

import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.match.command.application.match.MatchService;
import kr.co.deepple.match.presentation.dto.MatchRequestDto;
import kr.co.deepple.match.presentation.dto.MatchResponseDto;
import kr.co.deepple.match.presentation.dto.MatchViews;
import kr.co.deepple.match.query.MatchQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "메시지(매칭) 관리 API")
@RestController
@RequestMapping("/match")
@RequiredArgsConstructor
public class MatchController {
    private final MatchService matchService;
    private final MatchQueryService matchQueryService;

    @Operation(summary = "메시지 전송 (매칭 요청)")
    @PostMapping("/request")
    public ResponseEntity<Void> requestMatch(@RequestBody MatchRequestDto matchRequestDto,
        @CurrentMemberId long memberId) {
        matchService.request(memberId, matchRequestDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "메시지 응답 (매칭 수락)")
    @PatchMapping("/{matchId}/approve")
    public ResponseEntity<Void> approveMatch(@PathVariable Long matchId,
        @RequestBody MatchResponseDto matchResponseDto, @CurrentMemberId long memberId) {
        matchService.approve(matchId, memberId, matchResponseDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "메시지 거절 (매칭 거절)")
    @PatchMapping("/{matchId}/reject")
    public ResponseEntity<Void> rejectMatch(@PathVariable Long matchId,
        @CurrentMemberId long memberId) {
        matchService.reject(matchId, memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "메시지 읽음 처리 (매칭 읽음 처리)")
    @PatchMapping("/{matchId}/check")
    public ResponseEntity<Void> check(@PathVariable Long matchId,
        @CurrentMemberId long memberId) {
        matchService.rejectCheck(memberId, matchId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "받은 매칭 메시지 읽음 처리")
    @PatchMapping("/{matchId}/read")
    public ResponseEntity<Void> readMatch(@PathVariable Long matchId,
        @CurrentMemberId long memberId) {
        matchService.readReceivedMatch(matchId, memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내가 보낸 매칭 메세지")
    @GetMapping("/sent")
    public ResponseEntity<MatchViews> getSentMatch(@RequestParam(required = false) Long lastMatchId,
        @CurrentMemberId long memberId) {
        return ResponseEntity.ok(matchQueryService.getSentMatches(memberId, lastMatchId));
    }

    @Operation(summary = "내가 받은 매칭 메세지")
    @GetMapping("/received")
    public ResponseEntity<MatchViews> getReceivedMatch(@RequestParam(required = false) Long lastMatchId,
        @CurrentMemberId long memberId) {
        return ResponseEntity.ok(matchQueryService.getReceivedMatches(memberId, lastMatchId));
    }
}
