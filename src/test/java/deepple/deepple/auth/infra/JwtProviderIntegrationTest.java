package deepple.deepple.auth.infra;

import deepple.deepple.common.enums.Role;
import deepple.deepple.common.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("access token은 RS256으로 서명하고 RFC 9068 헤더와 claim을 담습니다.")
    void createAccessToken() {
        // given
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        // when
        Jwt jwt = jwtDecoder.decode(jwtProvider.createAccessToken(1L, Role.MEMBER, now));

        // then
        assertThat(jwt.getHeaders())
            .containsEntry("alg", "RS256")
            .containsEntry("typ", "at+jwt")
            .containsKey("kid");
        assertThat(jwt.getIssuer()).hasToString(jwtProperties.issuer());
        assertThat(jwt.getAudience()).containsExactly("deepple-api");
        assertThat(jwt.getSubject()).isEqualTo("1");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("MEMBER");
        assertThat(jwt.getId()).isNotBlank();
        assertThat(jwt.getIssuedAt()).isEqualTo(now);
        assertThat(jwt.getExpiresAt()).isEqualTo(now.plus(jwtProperties.accessTokenLifetime()));
    }

    @ParameterizedTest
    @CsvSource({"MEMBER, deepple-app", "ADMIN, deepple-admin", "SUPER_ADMIN, deepple-admin"})
    @DisplayName("access token의 client_id는 회원이면 앱, 관리자면 관리자 웹입니다.")
    void createAccessTokenWithClientId(Role role, String clientId) {
        // when
        Jwt jwt = jwtDecoder.decode(jwtProvider.createAccessToken(1L, role, Instant.now()));

        // then
        assertThat(jwt.getClaimAsString("client_id")).isEqualTo(clientId);
    }

    @Test
    @DisplayName("같은 시각에 발급해도 토큰이 서로 다릅니다.")
    void createDifferentTokensAtSameTime() {
        // given
        Instant now = Instant.now();

        // when
        String first = jwtProvider.createAccessToken(1L, Role.MEMBER, now);
        String second = jwtProvider.createAccessToken(1L, Role.MEMBER, now);

        // then
        assertThat(first).isNotEqualTo(second);
    }
}
