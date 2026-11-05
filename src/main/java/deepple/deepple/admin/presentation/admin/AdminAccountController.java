package deepple.deepple.admin.presentation.admin;

import deepple.deepple.admin.command.application.admin.AdminAccountService;
import deepple.deepple.admin.presentation.admin.dto.AdminApproveRequest;
import deepple.deepple.admin.query.admin.AdminAccountQueryRepository;
import deepple.deepple.admin.query.admin.PendingAdminView;
import deepple.deepple.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static deepple.deepple.common.enums.StatusType.OK;

@Tag(name = "관리자 계정 승인 API")
@RestController
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService adminAccountService;
    private final AdminAccountQueryRepository adminAccountQueryRepository;

    @Operation(summary = "승인 대기 관리자 목록 조회")
    @GetMapping
    public ResponseEntity<BaseResponse<List<PendingAdminView>>> getPendingAdmins() {
        return ResponseEntity.ok(BaseResponse.of(OK, adminAccountQueryRepository.findPendingAdmins()));
    }

    @Operation(summary = "관리자 승인")
    @PostMapping("/{adminId}/approve")
    public ResponseEntity<BaseResponse<Void>> approve(
        @PathVariable long adminId,
        @Valid @RequestBody AdminApproveRequest request
    ) {
        adminAccountService.approve(adminId, request.role());
        return ResponseEntity.ok(BaseResponse.from(OK));
    }

    @Operation(summary = "관리자 거절")
    @PostMapping("/{adminId}/reject")
    public ResponseEntity<BaseResponse<Void>> reject(@PathVariable long adminId) {
        adminAccountService.reject(adminId);
        return ResponseEntity.ok(BaseResponse.from(OK));
    }
}
