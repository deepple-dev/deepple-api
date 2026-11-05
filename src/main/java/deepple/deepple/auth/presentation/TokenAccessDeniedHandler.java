package deepple.deepple.auth.presentation;

import deepple.deepple.auth.domain.AuthErrorCode;
import deepple.deepple.common.exception.DeeppleException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@RequiredArgsConstructor
public class TokenAccessDeniedHandler implements AccessDeniedHandler {

    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
        AccessDeniedException accessDeniedException) {
        DeeppleException exception = new DeeppleException(
            AuthErrorCode.ACCESS_DENIED,
            accessDeniedException.getMessage(),
            accessDeniedException
        );
        handlerExceptionResolver.resolveException(request, response, null, exception);
    }
}
