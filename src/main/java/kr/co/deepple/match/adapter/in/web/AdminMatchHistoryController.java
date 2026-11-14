package kr.co.deepple.match.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.match.application.MatchHistoryQueryService;
import kr.co.deepple.match.application.dto.MatchHistorySearchCondition;
import kr.co.deepple.match.application.dto.MatchHistoryView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.MATCH)
@RestController
@RequestMapping("/admin/matches")
@RequiredArgsConstructor
public class AdminMatchHistoryController {

  private final MatchHistoryQueryService matchHistoryQueryService;

  @Operation(summary = "매치 내역 조회")
  @GetMapping
  public ResponseEntity<Page<MatchHistoryView>> getMatchHistories(
      @Valid @ModelAttribute MatchHistorySearchCondition matchHistorySearchCondition,
      @PageableDefault(size = 100) Pageable pageable) {
    return ResponseEntity.ok(
        matchHistoryQueryService.findMatchHistories(matchHistorySearchCondition, pageable));
  }
}
