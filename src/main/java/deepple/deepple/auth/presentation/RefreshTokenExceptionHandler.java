package deepple.deepple.auth.presentation;

import deepple.deepple.auth.domain.exception.InvalidRefreshTokenException;
import deepple.deepple.auth.domain.exception.MissingRefreshTokenException;
import deepple.deepple.common.enums.StatusType;
import deepple.deepple.common.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RefreshTokenExceptionHandler {

    @ExceptionHandler(MissingRefreshTokenException.class)
    public ResponseEntity<BaseResponse<Void>> handleMissingRefreshTokenException(MissingRefreshTokenException e) {
        log.warn("리프레시 토큰이 누락되었습니다. {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(BaseResponse.of(StatusType.MISSING_REFRESH_TOKEN, e.getMessage()));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<BaseResponse<Void>> handleInvalidRefreshTokenException(InvalidRefreshTokenException e) {
        log.warn("유효하지 않은 리프레시 토큰입니다. {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(BaseResponse.of(StatusType.INVALID_REFRESH_TOKEN, e.getMessage()));
    }
}
