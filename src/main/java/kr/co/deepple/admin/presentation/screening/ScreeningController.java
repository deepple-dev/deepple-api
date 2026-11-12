package kr.co.deepple.admin.presentation.screening;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.admin.command.application.screening.ScreeningService;
import kr.co.deepple.admin.query.screening.ScreeningDetailQueryRepository;
import kr.co.deepple.admin.query.screening.ScreeningDetailView;
import kr.co.deepple.admin.query.screening.ScreeningQueryRepository;
import kr.co.deepple.admin.query.screening.ScreeningSearchCondition;
import kr.co.deepple.admin.query.screening.ScreeningView;
import kr.co.deepple.common.security.CurrentAdminId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 페이지 심사 관리 API")
@RestController
@RequestMapping("/admin/screenings")
@RequiredArgsConstructor
public class ScreeningController {

  private final ScreeningService screeningService;
  private final ScreeningQueryRepository screeningQueryRepository;
  private final ScreeningDetailQueryRepository screeningDetailQueryRepository;

  @Operation(summary = "심사 목록 조회")
  @GetMapping
  public ResponseEntity<Page<ScreeningView>> getScreenings(
      @Valid @ModelAttribute ScreeningSearchCondition condition,
      @PageableDefault(size = 100) Pageable pageable) {
    return ResponseEntity.ok(screeningQueryRepository.findScreenings(condition, pageable));
  }

  @Operation(summary = "심사 상세 조회")
  @GetMapping("/{screeningId}")
  public ResponseEntity<ScreeningDetailView> getScreeningDetail(@PathVariable long screeningId) {
    return ResponseEntity.ok(screeningDetailQueryRepository.findById(screeningId));
  }

  @Operation(summary = "심사 승인")
  @PostMapping("/{screeningId}/approve")
  public ResponseEntity<Void> approve(
      @PathVariable long screeningId,
      @RequestBody ScreeningApproveRequest request,
      @CurrentAdminId long adminId) {
    screeningService.approve(screeningId, request, adminId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "심사 거절")
  @PostMapping("/{screeningId}/reject")
  public ResponseEntity<Void> reject(
      @PathVariable long screeningId,
      @Valid @RequestBody ScreeningRejectRequest request,
      @CurrentAdminId long adminId) {
    screeningService.reject(screeningId, adminId, request);
    return ResponseEntity.noContent().build();
  }
}
