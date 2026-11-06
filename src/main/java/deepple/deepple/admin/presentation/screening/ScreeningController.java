package deepple.deepple.admin.presentation.screening;

import deepple.deepple.admin.command.application.screening.ScreeningService;
import deepple.deepple.admin.query.screening.*;
import deepple.deepple.auth.presentation.CurrentAdminId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
        @PageableDefault(size = 100) Pageable pageable
    ) {
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
        @CurrentAdminId long adminId
    ) {
        screeningService.approve(screeningId, request, adminId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "심사 거절")
    @PostMapping("/{screeningId}/reject")
    public ResponseEntity<Void> reject(
        @PathVariable long screeningId,
        @Valid @RequestBody ScreeningRejectRequest request,
        @CurrentAdminId long adminId
    ) {
        screeningService.reject(screeningId, adminId, request);
        return ResponseEntity.noContent().build();
    }
}
