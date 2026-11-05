package deepple.deepple.common.exception;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String CODE_PROPERTY = "code";
    private static final String TRACE_ID_PROPERTY = "traceId";
    private static final String ERRORS_PROPERTY = "errors";
    private static final HttpStatusCode CLIENT_CLOSED_REQUEST = HttpStatusCode.valueOf(499);
    private static final String INTERNAL_ERROR_CODE = HttpStatus.INTERNAL_SERVER_ERROR.name();
    private static final String INTERNAL_ERROR_MESSAGE = "서버 내부 오류가 발생했습니다.";
    private static final String CONCURRENCY_FAILURE_MESSAGE = "다른 요청과 동시에 처리되어 실패했습니다. 다시 시도해 주세요.";
    private static final Set<Integer> WARN_STATUSES = Set.of(
        HttpStatus.BAD_REQUEST.value(),
        HttpStatus.UNAUTHORIZED.value(),
        HttpStatus.FORBIDDEN.value()
    );

    private final ObjectProvider<Tracer> tracerProvider;

    @ExceptionHandler(DeeppleException.class)
    public ResponseEntity<ProblemDetail> handleDeeppleException(DeeppleException e, HttpServletRequest request) {
        return respond(e.getErrorCode(), e, request);
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<ProblemDetail> handleConcurrencyFailure(
        ConcurrencyFailureException e,
        HttpServletRequest request
    ) {
        return respond(HttpStatus.CONFLICT, HttpStatus.CONFLICT.name(), CONCURRENCY_FAILURE_MESSAGE, e, request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleEntityNotFoundException(
        EntityNotFoundException e,
        HttpServletRequest request
    ) {
        return respond(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.name(), e.getMessage(), e, request);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ProblemDetail> handleIllegalArgumentOrState(RuntimeException e, HttpServletRequest request) {
        return respond(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.name(), e.getMessage(), e, request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ProblemDetail> handleDataAccessException(DataAccessException e, HttpServletRequest request) {
        if (e.getMostSpecificCause() instanceof IllegalStateException cause) {
            return handleIllegalArgumentOrState(cause, request);
        }
        return handleUnexpectedException(e, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(Exception e, HttpServletRequest request) {
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR_CODE, INTERNAL_ERROR_MESSAGE, e, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
        HttpRequestMethodNotSupportedException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        return handleExceptionInternal(ex, null, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
        HttpMessageNotReadableException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        if (ex.getMostSpecificCause() instanceof UnrecognizedPropertyException cause) {
            ProblemDetail body = ProblemDetail.forStatusAndDetail(status, "알 수 없는 필드입니다: " + cause.getPropertyName());
            return handleExceptionInternal(ex, body, headers, status, request);
        }
        return super.handleHttpMessageNotReadable(ex, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        ResponseEntity<Object> response = super.handleMethodArgumentNotValid(ex, headers, status, request);
        if (response != null && response.getBody() instanceof ProblemDetail problemDetail) {
            List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> Map.of("field", error.getField(), "message", String.valueOf(error.getDefaultMessage())))
                .toList();
            problemDetail.setProperty(ERRORS_PROPERTY, errors);
        }
        return response;
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception ex,
        Object body,
        HttpHeaders headers,
        HttpStatusCode statusCode,
        WebRequest request
    ) {
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        String code = resolveCode(statusCode);
        if (response != null && response.getBody() instanceof ProblemDetail problemDetail) {
            addProperties(problemDetail, code);
        }
        HttpServletRequest servletRequest = ((NativeWebRequest) request).getNativeRequest(HttpServletRequest.class);
        logException(statusCode, code, ex, servletRequest);
        return response;
    }

    private ResponseEntity<ProblemDetail> respond(ErrorCode errorCode, Exception e, HttpServletRequest request) {
        return respond(toHttpStatus(errorCode.getType()), errorCode.getCode(), errorCode.getMessage(), e, request);
    }

    private ResponseEntity<ProblemDetail> respond(
        HttpStatusCode status,
        String code,
        String detail,
        Exception e,
        HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        addProperties(problemDetail, code);
        logException(status, code, e, request);
        return ResponseEntity.status(status).body(problemDetail);
    }

    private HttpStatusCode toHttpStatus(ErrorType type) {
        return switch (type) {
            case CANCELLED -> CLIENT_CLOSED_REQUEST;
            case INVALID_ARGUMENT, FAILED_PRECONDITION, OUT_OF_RANGE -> HttpStatus.BAD_REQUEST;
            case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
            case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ALREADY_EXISTS, ABORTED -> HttpStatus.CONFLICT;
            case RESOURCE_EXHAUSTED -> HttpStatus.TOO_MANY_REQUESTS;
            case UNKNOWN, INTERNAL, DATA_LOSS -> HttpStatus.INTERNAL_SERVER_ERROR;
            case UNIMPLEMENTED -> HttpStatus.NOT_IMPLEMENTED;
            case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case DEADLINE_EXCEEDED -> HttpStatus.GATEWAY_TIMEOUT;
        };
    }

    private String resolveCode(HttpStatusCode statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        if (status == null) {
            return String.valueOf(statusCode.value());
        }
        return status.name();
    }

    private void addProperties(ProblemDetail problemDetail, String code) {
        problemDetail.setProperty(CODE_PROPERTY, code);
        String traceId = findTraceId();
        if (traceId != null) {
            problemDetail.setProperty(TRACE_ID_PROPERTY, traceId);
        }
    }

    private String findTraceId() {
        Tracer tracer = tracerProvider.getIfAvailable();
        if (tracer == null) {
            return null;
        }
        Span span = tracer.currentSpan();
        if (span == null) {
            return null;
        }
        return span.context().traceId();
    }

    private void logException(HttpStatusCode status, String code, Exception e, HttpServletRequest request) {
        String method = request == null ? null : request.getMethod();
        String uri = request == null ? null : request.getRequestURI();
        if (status.is5xxServerError()) {
            log.error("[{}] {} {} - {}", code, method, uri, e.getMessage(), e);
            return;
        }
        if (WARN_STATUSES.contains(status.value())) {
            log.warn("[{}] {} {} - {}", code, method, uri, e.getMessage());
            return;
        }
        log.info("[{}] {} {} - {}", code, method, uri, e.getMessage());
    }
}
