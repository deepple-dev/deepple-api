package deepple.deepple.auth.presentation.filter;


import deepple.deepple.auth.application.AuthResponse;
import deepple.deepple.auth.application.AuthService;
import deepple.deepple.auth.presentation.AuthContext;
import deepple.deepple.auth.presentation.RefreshTokenCookieProperties;
import deepple.deepple.common.enums.Role;
import deepple.deepple.common.enums.StatusType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class TokenFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final PathMatcherHelper pathMatcherHelper;
    private final TokenExtractor tokenExtractor;
    private final AuthService authService;
    private final AuthContext authContext;
    private final ResponseHandler responseHandler;
    private final RefreshTokenCookieProperties refreshTokenCookieProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (isExcluded(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = tokenExtractor.extractAccessToken(request);
        String refreshToken = tokenExtractor.extractRefreshToken(request);

        AuthResponse authResponse = authService.authenticate(accessToken, refreshToken);

        if (authResponse.isAuthenticated()) {
            setAuthContext(authResponse.getMemberId(), authResponse.getRole());
            filterChain.doFilter(request, response);
            return;
        }

        if (authResponse.isReissued()) {
            addAccessTokenToHeader(response, authResponse.getAccessToken());
            addRefreshTokenToCookie(response, authResponse.getRefreshToken());
            return;
        }

        if (authResponse.isError()) {
            setUnauthorizedResponse(response, StatusType.valueOf(authResponse.getErrorStatus().toString()));
        }
    }

    private boolean isExcluded(String uri) {
        return pathMatcherHelper.isExcluded(uri);
    }

    private void setAuthContext(long id, Role role) {
        authContext.authenticate(id, role);
    }

    private void addAccessTokenToHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(AUTHORIZATION, BEARER_PREFIX + accessToken);
    }

    private void addRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(refreshTokenCookieProperties.name(), refreshToken);
        cookie.setMaxAge(refreshTokenCookieProperties.maxAge());
        cookie.setPath(refreshTokenCookieProperties.path());
        cookie.setHttpOnly(refreshTokenCookieProperties.httpOnly());
        cookie.setSecure(refreshTokenCookieProperties.secure());

        response.addCookie(cookie);
    }

    private void setUnauthorizedResponse(HttpServletResponse response, StatusType statusType) {
        log.warn("[토큰 인증 실패] {}", statusType.getMessage());
        responseHandler.setResponse(response, statusType);
    }
}