package deepple.deepple.common.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DeeppleException extends RuntimeException {

    @Getter
    private final ErrorCode errorCode;
    private final Map<String, Object> properties = new LinkedHashMap<>();

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

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public DeeppleException withProperty(String name, Object value) {
        properties.put(name, value);
        return this;
    }
}
