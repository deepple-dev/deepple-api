package deepple.deepple.common.exception;

public interface ErrorCode {

    ErrorType getType();

    String getCode();

    String getMessage();
}
