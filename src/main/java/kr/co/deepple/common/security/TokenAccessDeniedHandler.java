package kr.co.deepple.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.deepple.common.exception.DeeppleException;
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
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException) {
    DeeppleException exception =
        new DeeppleException(
            SecurityErrorCode.ACCESS_DENIED,
            accessDeniedException.getMessage(),
            accessDeniedException);
    handlerExceptionResolver.resolveException(request, response, null, exception);
  }
}
