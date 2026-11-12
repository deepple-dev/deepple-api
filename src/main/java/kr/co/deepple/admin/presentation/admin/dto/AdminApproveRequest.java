package kr.co.deepple.admin.presentation.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import kr.co.deepple.admin.command.domain.admin.AdminRole;

public record AdminApproveRequest(
    @Schema(implementation = AdminRole.class) @NotBlank(message = "관리자 역할은 필수입니다.") String role) {}
