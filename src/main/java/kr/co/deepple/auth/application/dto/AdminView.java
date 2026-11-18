package kr.co.deepple.auth.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.co.deepple.auth.domain.AdminRole;
import kr.co.deepple.auth.domain.ApprovalStatus;

public record AdminView(
    long adminId,
    String email,
    String name,
    String phoneNumber,
    String comment,
    @Schema(implementation = AdminRole.class) String role,
    @Schema(implementation = ApprovalStatus.class) String approvalStatus,
    LocalDateTime createdAt) {
  @QueryProjection
  public AdminView {}
}
