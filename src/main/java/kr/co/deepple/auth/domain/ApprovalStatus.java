package kr.co.deepple.auth.domain;

import java.util.Locale;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApprovalStatus {
  APPROVED("승인"),
  PENDING("미승인");

  private final String description;

  public static ApprovalStatus from(String value) {
    try {
      return ApprovalStatus.valueOf(value.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new DeeppleException(AuthErrorCode.INVALID_APPROVAL_STATUS, "value=" + value, e);
    }
  }
}
