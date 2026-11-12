package kr.co.deepple.admin.command.domain.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApprovalStatus {
  APPROVED("승인"),
  PENDING("미승인");

  private final String description;
}
