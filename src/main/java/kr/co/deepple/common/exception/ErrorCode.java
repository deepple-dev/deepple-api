package kr.co.deepple.common.exception;

public interface ErrorCode {

    ErrorType getType();

    String getMessage();

    String name();

    default String getCode() {
        return name();
    }
}
