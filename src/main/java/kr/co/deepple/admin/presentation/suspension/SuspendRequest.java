package kr.co.deepple.admin.presentation.suspension;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import kr.co.deepple.admin.command.domain.suspension.SuspensionStatus;

public record SuspendRequest(
    @Schema(description = "정지 상태", implementation = SuspensionStatus.class)
        @NotBlank(message = "정지 상태를 입력해주세요.")
        String status) {}
