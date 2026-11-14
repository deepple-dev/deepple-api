package kr.co.deepple.profile.domain;

import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProfileErrorCode implements ErrorCode {
  PROFILE_ACCESS_DENIED(ErrorType.PERMISSION_DENIED, "프로필에 접근할 권한이 없습니다.");

  private final ErrorType type;
  private final String message;
}
