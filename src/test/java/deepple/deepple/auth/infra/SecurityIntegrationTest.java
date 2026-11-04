package deepple.deepple.auth.infra;

import deepple.deepple.auth.domain.TokenProvider;
import deepple.deepple.common.enums.Role;
import deepple.deepple.common.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityIntegrationTest extends IntegrationTestSupport {

    private static final String ADMIN_API = "/admin/members";
    private static final String MEMBER_API = "/member/profile/contact";
    private static final String ALLOWED_ORIGIN = "https://admin.deepple.test";

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private TokenProvider tokenProvider;

    @Nested
    @DisplayName("access token 검증")
    class AccessTokenValidation {

        @Test
        @DisplayName("토큰이 없으면 401과 MISSING_ACCESS_TOKEN 코드를 응답한다.")
        void rejectMissingToken() {
            MvcTestResult result = mockMvc.get()
                .uri(MEMBER_API)
                .exchange();

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
            assertThat(result)
                .bodyJson()
                .extractingPath("$.code")
                .isEqualTo("401001");
        }

        @Test
        @DisplayName("만료된 토큰이면 401과 EXPIRED_ACCESS_TOKEN 코드를 응답한다.")
        void rejectExpiredToken() {
            String expiredToken = tokenProvider.createAccessToken(1L, Role.MEMBER,
                Instant.now().minus(Duration.ofDays(1)));

            MvcTestResult result = mockMvc.get()
                .uri(MEMBER_API)
                .header(HttpHeaders.AUTHORIZATION, bearer(expiredToken))
                .exchange();

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
            assertThat(result)
                .bodyJson()
                .extractingPath("$.code")
                .isEqualTo("401006");
        }

        @Test
        @DisplayName("다른 키로 서명한 토큰이면 401과 INVALID_ACCESS_TOKEN 코드를 응답한다.")
        void rejectTokenSignedWithOtherKey() {
            TokenProvider otherKeyProvider = new JwtProvider("other-secret-key-value-for-test-at-least-32-bytes",
                1800, 1209600);
            String forgedToken = otherKeyProvider.createAccessToken(1L, Role.ADMIN, Instant.now());

            MvcTestResult result = mockMvc.get()
                .uri(ADMIN_API)
                .header(HttpHeaders.AUTHORIZATION, bearer(forgedToken))
                .exchange();

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
            assertThat(result)
                .bodyJson()
                .extractingPath("$.code")
                .isEqualTo("401002");
        }
    }

    @Nested
    @DisplayName("역할별 접근")
    class RoleAuthorization {

        @Test
        @DisplayName("회원 토큰으로 관리자 API를 호출하면 403을 응답한다.")
        void rejectMemberOnAdminApi() {
            MvcTestResult result = mockMvc.get()
                .uri(ADMIN_API)
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(Role.MEMBER)))
                .exchange();

            assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
            assertThat(result)
                .bodyJson()
                .extractingPath("$.code")
                .isEqualTo("403001");
        }

        @Test
        @DisplayName("관리자 토큰으로 회원 API를 호출하면 403을 응답한다.")
        void rejectAdminOnMemberApi() {
            MvcTestResult result = mockMvc.get()
                .uri(MEMBER_API)
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(Role.ADMIN)))
                .exchange();

            assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("관리자 토큰으로 관리자 API를 호출할 수 있다.")
        void allowAdminOnAdminApi() {
            MvcTestResult result = mockMvc.get()
                .uri(ADMIN_API)
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(Role.ADMIN)))
                .exchange();

            assertThat(result).hasStatusOk();
        }

        @Test
        @DisplayName("로그아웃은 토큰 없이 호출할 수 없다.")
        void rejectLogoutWithoutToken() {
            MvcTestResult result = mockMvc.get()
                .uri("/member/logout")
                .exchange();

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("공개 경로")
    class PublicPath {

        @Test
        @DisplayName("만료된 토큰이 함께 와도 토큰을 검사하지 않는다.")
        void ignoreTokenOnPublicPath() {
            String expiredToken = tokenProvider.createAccessToken(1L, Role.MEMBER,
                Instant.now().minus(Duration.ofDays(1)));

            MvcTestResult result = mockMvc.post()
                .uri("/member/refresh")
                .header(HttpHeaders.AUTHORIZATION, bearer(expiredToken))
                .exchange();

            assertThat(result)
                .bodyJson()
                .extractingPath("$.code")
                .isEqualTo("401003");
        }

        @Test
        @DisplayName("공개 경로라도 공개한 메서드가 아니면 토큰을 요구한다.")
        void requireTokenForOtherMethodOnPublicPath() {
            MvcTestResult result = mockMvc.get()
                .uri("/member/login")
                .exchange();

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("헬스 체크는 토큰 없이 호출할 수 있다.")
        void allowHealthCheck() {
            MvcTestResult result = mockMvc.get()
                .uri("/actuator/health")
                .exchange();

            assertThat(result).hasStatusOk();
        }
    }

    @Nested
    @DisplayName("CORS")
    class Cors {

        @Test
        @DisplayName("허용한 Origin의 preflight 요청을 허용한다.")
        void allowPreflightFromAllowedOrigin() {
            MvcTestResult result = mockMvc.options()
                .uri(ADMIN_API)
                .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .exchange();

            assertThat(result).hasStatusOk();
            assertThat(result)
                .headers()
                .hasValue(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN)
                .hasValue(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }

        @Test
        @DisplayName("허용하지 않은 Origin의 요청은 403으로 거부한다.")
        void rejectRequestFromUnknownOrigin() {
            MvcTestResult result = mockMvc.get()
                .uri(ADMIN_API)
                .header(HttpHeaders.ORIGIN, "https://evil.example")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(Role.ADMIN)))
                .exchange();

            assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
            assertThat(result)
                .headers()
                .doesNotContainHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
        }
    }

    private String accessToken(Role role) {
        return tokenProvider.createAccessToken(1L, role, Instant.now());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
