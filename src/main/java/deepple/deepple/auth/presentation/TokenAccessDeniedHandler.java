package deepple.deepple.auth.presentation;

import deepple.deepple.common.enums.StatusType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenAccessDeniedHandler implements AccessDeniedHandler {

    private final ResponseHandler responseHandler;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
        AccessDeniedException accessDeniedException) {
        log.warn("[권한 없음] uri={}", request.getRequestURI());
        responseHandler.setResponse(response, StatusType.HANDLE_ACCESS_DENIED);
    }
}
