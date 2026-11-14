package kr.co.deepple.report.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.security.CurrentAdminId;
import kr.co.deepple.report.adapter.out.persistence.ReportQueryRepository;
import kr.co.deepple.report.application.AdminReportService;
import kr.co.deepple.report.application.dto.ReportDetailView;
import kr.co.deepple.report.application.dto.ReportResultUpdateRequest;
import kr.co.deepple.report.application.dto.ReportSearchCondition;
import kr.co.deepple.report.application.dto.ReportView;
import kr.co.deepple.report.domain.ReportErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "어드민 신고 관리 API")
@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

  private final AdminReportService adminReportService;
  private final ReportQueryRepository reportQueryRepository;

  @Operation(summary = "신고 처리 결정")
  @PatchMapping("/{id}/result")
  public ResponseEntity<Void> updateResult(
      @PathVariable long id,
      @Valid @RequestBody ReportResultUpdateRequest request,
      @CurrentAdminId long adminId) {
    adminReportService.updateResult(id, request, adminId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "신고 목록 조회")
  @GetMapping
  public ResponseEntity<Page<ReportView>> getPage(
      @ModelAttribute ReportSearchCondition condition,
      @PageableDefault(size = 100) Pageable pageable) {
    Page<ReportView> page = reportQueryRepository.getPage(condition, pageable);
    return ResponseEntity.ok(page);
  }

  @Operation(summary = "신고 상세 조회")
  @GetMapping("/{id}")
  public ResponseEntity<ReportDetailView> getReport(@PathVariable long id) {
    ReportDetailView view =
        reportQueryRepository
            .findReportDetailView(id)
            .orElseThrow(
                () -> new DeeppleException(ReportErrorCode.REPORT_NOT_FOUND, "reportId=" + id));
    return ResponseEntity.ok(view);
  }
}
