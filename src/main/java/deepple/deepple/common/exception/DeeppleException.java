package deepple.deepple.common.exception;

import lombok.Getter;

@Getter
public class DeeppleException extends RuntimeException {

    private final ErrorCode errorCode;

    public DeeppleException(ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage());
    }

    public DeeppleException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
    }

    public DeeppleException(ErrorCode errorCode, String detail, Throwable cause) {
        super(detail, cause);
        this.errorCode = errorCode;
    }
}
