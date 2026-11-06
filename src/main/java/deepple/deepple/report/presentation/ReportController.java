package deepple.deepple.report.presentation;

import deepple.deepple.auth.presentation.CurrentMemberId;
import deepple.deepple.report.command.application.ReportService;
import deepple.deepple.report.presentation.dto.ReportCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "신고 API")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @Operation(summary = "신고하기")
    @PostMapping
    public ResponseEntity<Void> report(@Valid @RequestBody ReportCreateRequest request,
        @CurrentMemberId long memberId) {
        reportService.report(request, memberId);
        return ResponseEntity.noContent().build();
    }
}
