package kr.co.deepple.admin.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.admin.application.WarningService;
import kr.co.deepple.admin.application.dto.WarningCreateRequest;
import kr.co.deepple.common.security.CurrentAdminId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 경고 API")
@RestController
@RequestMapping("/admin/warnings")
@RequiredArgsConstructor
public class WarningController {

  private final WarningService warningService;

  @Operation(summary = "경고 부여")
  @PostMapping
  public ResponseEntity<Void> issue(
      @CurrentAdminId long adminId, @Valid @RequestBody WarningCreateRequest request) {
    warningService.issue(adminId, request);
    return ResponseEntity.noContent().build();
  }
}
