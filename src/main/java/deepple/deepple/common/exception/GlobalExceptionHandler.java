package deepple.deepple.common.exception;

import deepple.deepple.common.enums.StatusType;
import deepple.deepple.common.response.BaseResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<List<String>>> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException e
    ) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .toList();

        return ResponseEntity.badRequest()
            .body(BaseResponse.of(StatusType.BAD_REQUEST, errors));
    }

    @ExceptionHandler(UnrecognizedPropertyException.class)
    public ResponseEntity<BaseResponse<String>> handleUnrecognizedPropertyException(
        UnrecognizedPropertyException e
    ) {
        String errorMessage = e.getMessage();

        return ResponseEntity.badRequest()
            .body(BaseResponse.of(StatusType.BAD_REQUEST, errorMessage));
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<String>> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException e
    ) {
        String errorMessage = e.getMessage();

        return ResponseEntity.badRequest()
            .body(BaseResponse.of(StatusType.BAD_REQUEST, errorMessage));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<BaseResponse<Void>> handleOptimisticLockingFailureException(
        OptimisticLockingFailureException e
    ) {
        log.warn("Optimistic locking failure exception", e);

        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(BaseResponse.from(StatusType.CONFLICT));
    }

    @ExceptionHandler(CannotGetLockException.class)
    public ResponseEntity<BaseResponse<Void>> handleCannotGetLockException(CannotGetLockException e) {
        log.warn("Can not Get NamedLock Exception", e);

        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(BaseResponse.from(StatusType.CONFLICT));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleEntityNotFoundException(EntityNotFoundException e) {
        log.warn("Entity not found exception", e);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(BaseResponse.of(StatusType.NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal argument exception", e);

        return ResponseEntity.badRequest()
            .body(BaseResponse.of(StatusType.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<BaseResponse<Void>> handleIllegalStateException(IllegalStateException e) {
        log.warn("Illegal state exception", e);

        return ResponseEntity.badRequest()
            .body(BaseResponse.of(StatusType.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<BaseResponse<Void>> handleDataAccessException(DataAccessException e) {
        Throwable cause = e.getMostSpecificCause();

        if (cause instanceof IllegalStateException) {
            log.warn("Illegal state exception", cause);
            return ResponseEntity.badRequest()
                .body(BaseResponse.of(StatusType.BAD_REQUEST, cause.getMessage()));
        }

        log.error("Data access exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(BaseResponse.from(StatusType.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleGlobalException(Exception exception, HttpServletRequest request) {
        String requestInfo = "Request URI: " + request.getRequestURI() + ", Method: " + request.getMethod();
        log.error("Unexpected error occurred. RequestInfo: {}, ExceptionMessage: {}", requestInfo,
            exception.getMessage(), exception);

        return ResponseEntity.internalServerError()
            .body(BaseResponse.from(StatusType.INTERNAL_SERVER_ERROR));
    }
}
