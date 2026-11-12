package kr.co.deepple.admin.presentation.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.admin.command.application.admin.AdminAccountService;
import kr.co.deepple.admin.presentation.admin.dto.AdminApproveRequest;
import kr.co.deepple.admin.presentation.admin.dto.PendingAdminViews;
import kr.co.deepple.admin.query.admin.AdminAccountQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 계정 승인 API")
@RestController
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {

  private final AdminAccountService adminAccountService;
  private final AdminAccountQueryRepository adminAccountQueryRepository;

  @Operation(summary = "승인 대기 관리자 목록 조회")
  @GetMapping
  public ResponseEntity<PendingAdminViews> getPendingAdmins() {
    return ResponseEntity.ok(
        new PendingAdminViews(adminAccountQueryRepository.findPendingAdmins()));
  }

  @Operation(summary = "관리자 승인")
  @PostMapping("/{adminId}/approve")
  public ResponseEntity<Void> approve(
      @PathVariable long adminId, @Valid @RequestBody AdminApproveRequest request) {
    adminAccountService.approve(adminId, request.role());
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "관리자 거절")
  @PostMapping("/{adminId}/reject")
  public ResponseEntity<Void> reject(@PathVariable long adminId) {
    adminAccountService.reject(adminId);
    return ResponseEntity.noContent().build();
  }
}
