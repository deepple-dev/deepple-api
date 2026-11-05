package deepple.deepple.auth.presentation;

import deepple.deepple.auth.domain.AuthErrorCode;
import deepple.deepple.auth.infra.JwtExpirationValidator;
import deepple.deepple.common.exception.DeeppleException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@RequiredArgsConstructor
public class TokenAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) {
        AuthErrorCode errorCode = resolveErrorCode(authException);
        DeeppleException exception = new DeeppleException(errorCode, authException.getMessage(), authException);
        handlerExceptionResolver.resolveException(request, response, null, exception);
    }

    private AuthErrorCode resolveErrorCode(AuthenticationException exception) {
        if (!(exception instanceof InvalidBearerTokenException)) {
            return AuthErrorCode.MISSING_ACCESS_TOKEN;
        }
        if (isExpired(exception)) {
            return AuthErrorCode.EXPIRED_ACCESS_TOKEN;
        }
        return AuthErrorCode.INVALID_ACCESS_TOKEN;
    }

    private boolean isExpired(AuthenticationException exception) {
        return exception.getCause() instanceof JwtValidationException validationException
            && validationException.getErrors()
            .stream()
            .anyMatch(error -> JwtExpirationValidator.EXPIRED_ERROR_CODE.equals(error.getErrorCode()));
    }
}
