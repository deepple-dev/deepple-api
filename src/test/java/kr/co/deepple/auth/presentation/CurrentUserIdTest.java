package kr.co.deepple.auth.presentation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentUserIdTest {

    private final AuthenticationPrincipalArgumentResolver resolver = new AuthenticationPrincipalArgumentResolver();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("@CurrentMemberId는 토큰의 sub를 회원 ID로 넘긴다.")
    @Test
    void resolveMemberId() throws Exception {
        authenticate("7");

        Object memberId = resolve("member");

        assertThat(memberId).isEqualTo(7L);
    }

    @DisplayName("@CurrentAdminId는 토큰의 sub를 관리자 ID로 넘긴다.")
    @Test
    void resolveAdminId() throws Exception {
        authenticate("3");

        Object adminId = resolve("admin");

        assertThat(adminId).isEqualTo(3L);
    }

    private void authenticate(String subject) {
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .subject(subject)
            .build();
        SecurityContextHolder.getContext()
            .setAuthentication(new JwtAuthenticationToken(jwt));
    }

    private Object resolve(String methodName) throws Exception {
        MethodParameter parameter = new MethodParameter(
            Handlers.class.getDeclaredMethod(methodName, long.class), 0);
        assertThat(resolver.supportsParameter(parameter)).isTrue();
        return resolver.resolveArgument(parameter, null, null, null);
    }

    static class Handlers {

        void member(@CurrentMemberId long memberId) {
        }

        void admin(@CurrentAdminId long adminId) {
        }
    }
}
