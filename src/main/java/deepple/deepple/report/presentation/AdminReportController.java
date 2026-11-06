package deepple.deepple.report.presentation;

import deepple.deepple.auth.presentation.CurrentAdminId;
import deepple.deepple.common.enums.StatusType;
import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.common.response.BaseResponse;
import deepple.deepple.report.command.application.AdminReportService;
import deepple.deepple.report.command.domain.ReportErrorCode;
import deepple.deepple.report.presentation.dto.ReportResultUpdateRequest;
import deepple.deepple.report.query.ReportQueryRepository;
import deepple.deepple.report.query.condition.ReportSearchCondition;
import deepple.deepple.report.query.view.ReportDetailView;
import deepple.deepple.report.query.view.ReportView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "어드민 신고 관리 API")
@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {
    private final AdminReportService adminReportService;
    private final ReportQueryRepository reportQueryRepository;

    @Operation(summary = "신고 처리 결정")
    @PatchMapping("/{id}/result")
    public ResponseEntity<BaseResponse<Void>> updateResult(
        @PathVariable long id,
        @Valid @RequestBody ReportResultUpdateRequest request,
        @CurrentAdminId long adminId
    ) {
        adminReportService.updateResult(id, request, adminId);
        return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
    }

    @Operation(summary = "신고 목록 조회")
    @GetMapping
    public ResponseEntity<BaseResponse<Page<ReportView>>> getPage(
        @ModelAttribute ReportSearchCondition condition,
        @PageableDefault(size = 100) Pageable pageable
    ) {
        Page<ReportView> page = reportQueryRepository.getPage(condition, pageable);
        return ResponseEntity.ok(BaseResponse.of(StatusType.OK, page));
    }

    @Operation(summary = "신고 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ReportDetailView>> getReport(@PathVariable long id) {
        ReportDetailView view = reportQueryRepository.findReportDetailView(id)
            .orElseThrow(() -> new DeeppleException(ReportErrorCode.REPORT_NOT_FOUND, "reportId=" + id));
        return ResponseEntity.ok(BaseResponse.of(StatusType.OK, view));
    }
}
