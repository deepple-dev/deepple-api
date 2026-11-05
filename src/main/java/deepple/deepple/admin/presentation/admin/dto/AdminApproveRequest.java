package deepple.deepple.admin.presentation.admin.dto;

import deepple.deepple.admin.command.domain.admin.AdminRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AdminApproveRequest(
    @Schema(implementation = AdminRole.class)
    @NotBlank(message = "관리자 역할은 필수입니다.")
    String role
) {
}
