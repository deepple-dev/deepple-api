package deepple.deepple.heart.command.domain;

import deepple.deepple.common.exception.ErrorCode;
import deepple.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HeartErrorCode implements ErrorCode {
    INSUFFICIENT_HEART_BALANCE(ErrorType.FAILED_PRECONDITION, "하트가 부족합니다.");

    private final ErrorType type;
    private final String message;
}
