package kr.co.deepple.match.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.match.application.MatchQueryService;
import kr.co.deepple.match.application.MatchService;
import kr.co.deepple.match.application.dto.MatchRequestDto;
import kr.co.deepple.match.application.dto.MatchResponseDto;
import kr.co.deepple.match.application.dto.MatchViews;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.MATCH)
@RestController
@RequestMapping("/match")
@RequiredArgsConstructor
public class MatchController {

  private final MatchService matchService;
  private final MatchQueryService matchQueryService;

  @Operation(summary = "메시지 전송 (매칭 요청)")
  @PostMapping("/request")
  public ResponseEntity<Void> requestMatch(
      @RequestBody MatchRequestDto matchRequestDto, @CurrentMemberId long memberId) {
    matchService.request(memberId, matchRequestDto);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "메시지 응답 (매칭 수락)")
  @PatchMapping("/{matchId}/approve")
  public ResponseEntity<Void> approveMatch(
      @PathVariable long matchId,
      @RequestBody MatchResponseDto matchResponseDto,
      @CurrentMemberId long memberId) {
    matchService.approve(matchId, memberId, matchResponseDto);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "메시지 거절 (매칭 거절)")
  @PatchMapping("/{matchId}/reject")
  public ResponseEntity<Void> rejectMatch(
      @PathVariable long matchId, @CurrentMemberId long memberId) {
    matchService.reject(matchId, memberId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "메시지 읽음 처리 (매칭 읽음 처리)")
  @PatchMapping("/{matchId}/check")
  public ResponseEntity<Void> check(@PathVariable long matchId, @CurrentMemberId long memberId) {
    matchService.rejectCheck(memberId, matchId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "받은 매칭 메시지 읽음 처리")
  @PatchMapping("/{matchId}/read")
  public ResponseEntity<Void> readMatch(
      @PathVariable long matchId, @CurrentMemberId long memberId) {
    matchService.readReceivedMatch(matchId, memberId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "내가 보낸 매칭 메세지")
  @GetMapping("/sent")
  public ResponseEntity<MatchViews> getSentMatch(
      @RequestParam(required = false) Long lastMatchId, @CurrentMemberId long memberId) {
    return ResponseEntity.ok(matchQueryService.getSentMatches(memberId, lastMatchId));
  }

  @Operation(summary = "내가 받은 매칭 메세지")
  @GetMapping("/received")
  public ResponseEntity<MatchViews> getReceivedMatch(
      @RequestParam(required = false) Long lastMatchId, @CurrentMemberId long memberId) {
    return ResponseEntity.ok(matchQueryService.getReceivedMatches(memberId, lastMatchId));
  }
}
