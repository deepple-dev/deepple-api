package deepple.deepple.admin.presentation.suspension;

import deepple.deepple.admin.command.application.suspension.SuspensionService;
import deepple.deepple.auth.presentation.CurrentAdminId;
import deepple.deepple.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static deepple.deepple.common.enums.StatusType.OK;

@Tag(name = "관리자 회원 정지 API")
@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class SuspensionController {

    private final SuspensionService suspensionService;

    @Operation(description = "회원 정지 상태 변경")
    @PutMapping("/{memberId}/suspension")
    public ResponseEntity<BaseResponse<Void>> updateStatus(
        @PathVariable long memberId,
        @Valid @RequestBody SuspendRequest request,
        @CurrentAdminId long adminId
    ) {
        suspensionService.suspendByAdmin(adminId, memberId, request);
        return ResponseEntity.ok(BaseResponse.from(OK));
    }

    @Operation(description = "회원 정지 해제")
    @DeleteMapping("/{memberId}/suspension")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable long memberId) {
        suspensionService.delete(memberId);
        return ResponseEntity.ok(BaseResponse.from(OK));
    }
}
