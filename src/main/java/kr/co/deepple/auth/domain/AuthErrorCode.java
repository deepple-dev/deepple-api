package kr.co.deepple.auth.domain;

import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
  MISSING_REFRESH_TOKEN(ErrorType.UNAUTHENTICATED, "리프레시 토큰이 없습니다."),
  INVALID_REFRESH_TOKEN(ErrorType.UNAUTHENTICATED, "유효하지 않은 리프레시 토큰입니다.");

  private final ErrorType type;
  private final String message;
}
