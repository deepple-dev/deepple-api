package kr.co.deepple.common.security;

import kr.co.deepple.common.exception.DeeppleException;
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
        SecurityErrorCode errorCode = resolveErrorCode(authException);
        DeeppleException exception = new DeeppleException(errorCode, authException.getMessage(), authException);
        handlerExceptionResolver.resolveException(request, response, null, exception);
    }

    private SecurityErrorCode resolveErrorCode(AuthenticationException exception) {
        if (!(exception instanceof InvalidBearerTokenException)) {
            return SecurityErrorCode.MISSING_ACCESS_TOKEN;
        }
        if (isExpired(exception)) {
            return SecurityErrorCode.EXPIRED_ACCESS_TOKEN;
        }
        return SecurityErrorCode.INVALID_ACCESS_TOKEN;
    }

    private boolean isExpired(AuthenticationException exception) {
        return exception.getCause() instanceof JwtValidationException validationException
            && validationException.getErrors()
            .stream()
            .anyMatch(error -> JwtExpirationValidator.EXPIRED_ERROR_CODE.equals(error.getErrorCode()));
    }
}
