package deepple.deepple.auth.presentation.filter;

import deepple.deepple.auth.application.AuthErrorStatus;
import deepple.deepple.auth.application.AuthResponse;
import deepple.deepple.auth.application.AuthService;
import deepple.deepple.auth.presentation.AuthContext;
import deepple.deepple.common.enums.Role;
import deepple.deepple.common.enums.StatusType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenFilterTest {

    private static final String EXCLUDED_PATH = "/admin/login";
    private static final String PROTECTED_PATH = "/admin";

    private static final String VALID_ACCESS_TOKEN = "validAccessToken";
    private static final String INVALID_ACCESS_TOKEN = "invalidAccessToken";
    private static final String EXPIRED_ACCESS_TOKEN = "expiredAccessToken";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PathMatcherHelper pathMatcherHelper;

    @Mock
    private TokenExtractor tokenExtractor;

    @Mock
    private AuthService authService;

    @Mock
    private AuthContext authContext;

    @Mock
    private ResponseHandler responseHandler;

    @InjectMocks
    private TokenFilter tokenFilter;

    @Test
    @DisplayName("제외된 URI는 토큰을 검증하지 않고 필터를 통과합니다.")
    void shouldPassExcludedUriWithoutCheckingToken() throws IOException, ServletException {
        // given
        when(request.getRequestURI()).thenReturn(EXCLUDED_PATH);
        when(pathMatcherHelper.isExcluded(EXCLUDED_PATH)).thenReturn(true);

        // when
        tokenFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(tokenExtractor, authService, authContext, responseHandler);
    }

    @Test
    @DisplayName("Access token이 유효한 경우 AuthContext를 세팅하고 필터를 통과합니다.")
    void shouldSetAuthContextAndPassFilterWhenAccessTokenIsValid() throws IOException, ServletException {
        // given
        when(request.getRequestURI()).thenReturn(PROTECTED_PATH);
        when(pathMatcherHelper.isExcluded(PROTECTED_PATH)).thenReturn(false);

        when(tokenExtractor.extractAccessToken(request)).thenReturn(VALID_ACCESS_TOKEN);

        when(authService.authenticate(VALID_ACCESS_TOKEN))
            .thenReturn(AuthResponse.authenticated(1L, Role.MEMBER));

        // when
        tokenFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(authContext).authenticate(1L, Role.MEMBER);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(responseHandler);
    }

    @Test
    @DisplayName("Access token이 헤더에 없는 경우 401을 반환합니다.")
    void shouldReturnUnauthorizedIfAccessTokenIsMissing() throws IOException, ServletException {
        // given
        when(request.getRequestURI()).thenReturn(PROTECTED_PATH);
        when(pathMatcherHelper.isExcluded(PROTECTED_PATH)).thenReturn(false);

        when(tokenExtractor.extractAccessToken(request)).thenReturn(null);

        when(authService.authenticate(null))
            .thenReturn(AuthResponse.error(AuthErrorStatus.MISSING_ACCESS_TOKEN));

        // when
        tokenFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(responseHandler).setResponse(response, StatusType.MISSING_ACCESS_TOKEN);
        verifyNoInteractions(authContext, filterChain);
    }

    @Test
    @DisplayName("Access token이 유효하지 않은 경우 401을 반환합니다.")
    void shouldReturnUnauthorizedWhenAccessTokenIsInvalid() throws IOException, ServletException {
        // given
        when(request.getRequestURI()).thenReturn(PROTECTED_PATH);
        when(pathMatcherHelper.isExcluded(PROTECTED_PATH)).thenReturn(false);

        when(tokenExtractor.extractAccessToken(request)).thenReturn(INVALID_ACCESS_TOKEN);

        when(authService.authenticate(INVALID_ACCESS_TOKEN))
            .thenReturn(AuthResponse.error(AuthErrorStatus.INVALID_ACCESS_TOKEN));

        // when
        tokenFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(responseHandler).setResponse(response, StatusType.INVALID_ACCESS_TOKEN);
        verifyNoInteractions(authContext, filterChain);
    }

    @Test
    @DisplayName("Access token이 만료된 경우 401을 반환합니다.")
    void shouldReturnUnauthorizedWhenAccessTokenIsExpired() throws IOException, ServletException {
        // given
        when(request.getRequestURI()).thenReturn(PROTECTED_PATH);
        when(pathMatcherHelper.isExcluded(PROTECTED_PATH)).thenReturn(false);

        when(tokenExtractor.extractAccessToken(request)).thenReturn(EXPIRED_ACCESS_TOKEN);

        when(authService.authenticate(EXPIRED_ACCESS_TOKEN))
            .thenReturn(AuthResponse.error(AuthErrorStatus.EXPIRED_ACCESS_TOKEN));

        // when
        tokenFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(responseHandler).setResponse(response, StatusType.EXPIRED_ACCESS_TOKEN);
        verifyNoInteractions(authContext, filterChain);
    }
}