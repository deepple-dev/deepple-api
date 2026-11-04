package deepple.deepple.auth.presentation;

import deepple.deepple.auth.infra.JwtExpirationValidator;
import deepple.deepple.common.enums.StatusType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ResponseHandler responseHandler;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) {
        StatusType statusType = resolveStatusType(authException);
        log.warn("[토큰 인증 실패] {}, uri={}", statusType.getMessage(), request.getRequestURI());
        responseHandler.setResponse(response, statusType);
    }

    private StatusType resolveStatusType(AuthenticationException exception) {
        if (!(exception instanceof InvalidBearerTokenException)) {
            return StatusType.MISSING_ACCESS_TOKEN;
        }
        if (isExpired(exception)) {
            return StatusType.EXPIRED_ACCESS_TOKEN;
        }
        return StatusType.INVALID_ACCESS_TOKEN;
    }

    private boolean isExpired(AuthenticationException exception) {
        return exception.getCause() instanceof JwtValidationException validationException
            && validationException.getErrors()
            .stream()
            .anyMatch(error -> JwtExpirationValidator.EXPIRED_ERROR_CODE.equals(error.getErrorCode()));
    }
}
