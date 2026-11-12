package kr.co.deepple.auth.domain;

import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    MISSING_ACCESS_TOKEN(ErrorType.UNAUTHENTICATED, "액세스 토큰이 없습니다."),
    INVALID_ACCESS_TOKEN(ErrorType.UNAUTHENTICATED, "유효하지 않은 액세스 토큰입니다."),
    EXPIRED_ACCESS_TOKEN(ErrorType.UNAUTHENTICATED, "만료된 액세스 토큰입니다."),
    MISSING_REFRESH_TOKEN(ErrorType.UNAUTHENTICATED, "리프레시 토큰이 없습니다."),
    INVALID_REFRESH_TOKEN(ErrorType.UNAUTHENTICATED, "유효하지 않은 리프레시 토큰입니다."),
    ACCESS_DENIED(ErrorType.PERMISSION_DENIED, "접근 권한이 없습니다.");

    private final ErrorType type;
    private final String message;
}
