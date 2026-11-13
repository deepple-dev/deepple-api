package kr.co.deepple.auth.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;

public record PendingAdminView(
    long adminId,
    String email,
    String name,
    String phoneNumber,
    String comment,
    LocalDateTime createdAt) {
  @QueryProjection
  public PendingAdminView {}
}
