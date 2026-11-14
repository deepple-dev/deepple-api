package kr.co.deepple.admin.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.admin.application.SuspensionService;
import kr.co.deepple.admin.application.dto.SuspendRequest;
import kr.co.deepple.common.security.CurrentAdminId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 회원 정지 API")
@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class SuspensionController {

  private final SuspensionService suspensionService;

  @Operation(description = "회원 정지 상태 변경")
  @PutMapping("/{memberId}/suspension")
  public ResponseEntity<Void> updateStatus(
      @PathVariable long memberId,
      @Valid @RequestBody SuspendRequest request,
      @CurrentAdminId long adminId) {
    suspensionService.suspendByAdmin(adminId, memberId, request);
    return ResponseEntity.noContent().build();
  }

  @Operation(description = "회원 정지 해제")
  @DeleteMapping("/{memberId}/suspension")
  public ResponseEntity<Void> delete(@PathVariable long memberId) {
    suspensionService.delete(memberId);
    return ResponseEntity.noContent().build();
  }
}
