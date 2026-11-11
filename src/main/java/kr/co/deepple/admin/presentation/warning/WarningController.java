package kr.co.deepple.admin.presentation.warning;

import kr.co.deepple.admin.command.application.warning.WarningService;
import kr.co.deepple.auth.presentation.CurrentAdminId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
        @CurrentAdminId long adminId,
        @Valid @RequestBody WarningCreateRequest request
    ) {
        warningService.issue(adminId, request);
        return ResponseEntity.noContent().build();
    }
}
