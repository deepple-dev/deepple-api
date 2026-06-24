package deepple.deepple.match.presentation;

import deepple.deepple.auth.presentation.AuthContext;
import deepple.deepple.auth.presentation.AuthPrincipal;
import deepple.deepple.common.enums.StatusType;
import deepple.deepple.common.response.BaseResponse;
import deepple.deepple.match.command.application.match.MatchService;
import deepple.deepple.match.presentation.dto.MatchRequestDto;
import deepple.deepple.match.presentation.dto.MatchResponseDto;
import deepple.deepple.match.presentation.dto.MatchViews;
import deepple.deepple.match.query.MatchQueryService;
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
    public ResponseEntity<BaseResponse<Void>> requestMatch(@RequestBody MatchRequestDto matchRequestDto,
        @AuthPrincipal AuthContext authContext) {
        matchService.request(authContext.getId(), matchRequestDto);
        return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
    }

    @Operation(summary = "메시지 응답 (매칭 수락)")
    @PatchMapping("/{matchId}/approve")
    public ResponseEntity<BaseResponse<Void>> approveMatch(@PathVariable Long matchId,
        @RequestBody MatchResponseDto matchResponseDto, @AuthPrincipal AuthContext authContext) {
        matchService.approve(matchId, authContext.getId(), matchResponseDto);
        return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
    }

    @Operation(summary = "메시지 거절 (매칭 거절)")
    @PatchMapping("/{matchId}/reject")
    public ResponseEntity<BaseResponse<Void>> rejectMatch(@PathVariable Long matchId,
        @AuthPrincipal AuthContext authContext) {
        matchService.reject(matchId, authContext.getId());
        return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
    }

    @Operation(summary = "메시지 읽음 처리 (매칭 읽음 처리)")
    @PatchMapping("/{matchId}/check")
    public ResponseEntity<BaseResponse<Void>> check(@PathVariable Long matchId,
        @AuthPrincipal AuthContext authContext) {
        matchService.rejectCheck(authContext.getId(), matchId);
        return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
    }

    @Operation(summary = "받은 매칭 메시지 읽음 처리")
    @PatchMapping("/{matchId}/read")
    public ResponseEntity<BaseResponse<Void>> readMatch(@PathVariable Long matchId,
        @AuthPrincipal AuthContext authContext) {
        matchService.readReceivedMatch(matchId, authContext.getId());
        return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
    }

    @Operation(summary = "내가 보낸 매칭 메세지")
    @GetMapping("/sent")
    public ResponseEntity<BaseResponse<MatchViews>> getSentMatch(@RequestParam(required = false) Long lastMatchId,
        @AuthPrincipal AuthContext authContext) {
        return ResponseEntity.ok(
            BaseResponse.of(StatusType.OK, matchQueryService.getSentMatches(authContext.getId(), lastMatchId)));
    }

    @Operation(summary = "내가 받은 매칭 메세지")
    @GetMapping("/received")
    public ResponseEntity<BaseResponse<MatchViews>> getReceivedMatch(@RequestParam(required = false) Long lastMatchId,
        @AuthPrincipal AuthContext authContext) {
        return ResponseEntity.ok(
            BaseResponse.of(StatusType.OK, matchQueryService.getReceivedMatches(authContext.getId(), lastMatchId)));
    }
}
