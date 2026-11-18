package kr.co.deepple.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.auth.domain.AdminRole;
import kr.co.deepple.auth.domain.ApprovalStatus;

public record AdminSearchCondition(
    @Schema(implementation = ApprovalStatus.class) String approvalStatus,
    @Schema(implementation = AdminRole.class) String role,
    String name,
    String email) {}
