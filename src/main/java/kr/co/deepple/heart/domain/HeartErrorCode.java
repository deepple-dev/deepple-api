package kr.co.deepple.heart.domain;

import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HeartErrorCode implements ErrorCode {
    INSUFFICIENT_HEART_BALANCE(ErrorType.FAILED_PRECONDITION, "하트가 부족합니다."),
    MEMBER_NOT_FOUND(ErrorType.NOT_FOUND, "회원을 찾을 수 없습니다.");

    private final ErrorType type;
    private final String message;
}
