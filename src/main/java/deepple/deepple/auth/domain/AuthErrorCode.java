package deepple.deepple.auth.domain;

import deepple.deepple.common.exception.ErrorCode;
import deepple.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    MISSING_ACCESS_TOKEN(ErrorType.UNAUTHENTICATED, "MISSING_ACCESS_TOKEN", "액세스 토큰이 없습니다."),
    INVALID_ACCESS_TOKEN(ErrorType.UNAUTHENTICATED, "INVALID_ACCESS_TOKEN", "유효하지 않은 액세스 토큰입니다."),
    EXPIRED_ACCESS_TOKEN(ErrorType.UNAUTHENTICATED, "EXPIRED_ACCESS_TOKEN", "만료된 액세스 토큰입니다."),
    MISSING_REFRESH_TOKEN(ErrorType.UNAUTHENTICATED, "MISSING_REFRESH_TOKEN", "리프레시 토큰이 없습니다."),
    INVALID_REFRESH_TOKEN(ErrorType.UNAUTHENTICATED, "INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다."),
    ACCESS_DENIED(ErrorType.PERMISSION_DENIED, "ACCESS_DENIED", "접근 권한이 없습니다.");

    private final ErrorType type;
    private final String code;
    private final String message;
}
