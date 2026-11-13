package kr.co.deepple.auth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApprovalStatus {
  APPROVED("승인"),
  PENDING("미승인");

  private final String description;
}
