package kr.co.deepple.admin.presentation.warning;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;
import kr.co.deepple.admin.command.domain.warning.WarningReasonType;

public record WarningCreateRequest(
    long memberId,
    @Schema(implementation = WarningReasonType.class) @NotEmpty Set<String> reasonTypes,
    @Schema(description = "경고 여부 (true: 경고, false: 권고)") boolean isCritical,
    @Schema(
            description = "같은 경고가 두 번 부여되지 않게 하는 키. 요청마다 새로 만들고 재시도할 때는 같은 값을 보낸다",
            example = "0b7c2c1e-6f0a-4c8f-9a1b-3c5d7e9f1a2b")
        @Size(max = 100)
        @Pattern(regexp = "\\S+", message = "멱등 키에는 공백을 넣을 수 없습니다.")
        String idempotencyKey) {}
