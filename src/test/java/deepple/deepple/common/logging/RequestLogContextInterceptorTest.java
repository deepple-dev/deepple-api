package deepple.deepple.common.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerMapping;

import static org.assertj.core.api.Assertions.assertThat;

class RequestLogContextInterceptorTest {

    private final RequestLogContextInterceptor interceptor = new RequestLogContextInterceptor();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    @DisplayName("인증된 요청이면 회원 ID·역할·HTTP method·경로 템플릿을 MDC에 넣는다.")
    void putAuthenticatedRequestContext() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/match/3");
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/match/{id}");
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("42", null, "ROLE_MEMBER"));

        interceptor.preHandle(request, response, new Object());

        assertThat(MDC.get(LogContext.USER_ID)).isEqualTo("42");
        assertThat(MDC.get(LogContext.USER_ROLES)).isEqualTo("MEMBER");
        assertThat(MDC.get(LogContext.HTTP_REQUEST_METHOD)).isEqualTo("GET");
        assertThat(MDC.get(LogContext.HTTP_ROUTE)).isEqualTo("/match/{id}");
    }

    @Test
    @DisplayName("익명 요청이면 회원 정보를 MDC에 넣지 않는다.")
    void skipUserForAnonymousRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/member/login");
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
            "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        interceptor.preHandle(request, response, new Object());

        assertThat(MDC.get(LogContext.USER_ID)).isNull();
        assertThat(MDC.get(LogContext.USER_ROLES)).isNull();
        assertThat(MDC.get(LogContext.HTTP_REQUEST_METHOD)).isEqualTo("POST");
    }

    @Test
    @DisplayName("요청이 끝나면 넣었던 값을 MDC에서 지운다.")
    void clearContextAfterCompletion() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/match/3");
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/match/{id}");
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("42", null, "ROLE_MEMBER"));
        interceptor.preHandle(request, response, new Object());

        interceptor.afterCompletion(request, response, new Object(), null);

        assertThat(MDC.get(LogContext.USER_ID)).isNull();
        assertThat(MDC.get(LogContext.USER_ROLES)).isNull();
        assertThat(MDC.get(LogContext.HTTP_REQUEST_METHOD)).isNull();
        assertThat(MDC.get(LogContext.HTTP_ROUTE)).isNull();
    }
}
