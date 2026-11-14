package kr.co.deepple.report.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kr.co.deepple.report.domain.ReportResult;

public record ReportResultUpdateRequest(
    @NotNull(message = "버전을 입력해주세요.") Long version,
    @NotBlank(message = "신고 결과를 입력해주세요.") @Schema(implementation = ReportResult.class)
        String result,
    @Schema(
            description = "경고 처리가 두 번 반영되지 않게 하는 키. 요청마다 새로 만들고 재시도할 때는 같은 값을 보낸다",
            example = "0b7c2c1e-6f0a-4c8f-9a1b-3c5d7e9f1a2b")
        @Size(max = 100)
        @Pattern(regexp = "\\S+", message = "멱등 키에는 공백을 넣을 수 없습니다.")
        String idempotencyKey) {}
