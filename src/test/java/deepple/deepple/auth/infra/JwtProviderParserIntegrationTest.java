package deepple.deepple.auth.infra;

import deepple.deepple.common.enums.Role;
import deepple.deepple.common.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderParserIntegrationTest extends IntegrationTestSupport {

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private JwtParser jwtParser;

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
        assertThat(jwt.getIssuer()).hasToString(issuer);
        assertThat(jwt.getAudience()).containsExactly("deepple-api");
        assertThat(jwt.getSubject()).isEqualTo("1");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("MEMBER");
        assertThat(jwt.getId()).isNotBlank();
        assertThat(jwt.getIssuedAt()).isEqualTo(now);
        assertThat(jwt.getExpiresAt()).isEqualTo(now.plusSeconds(accessTokenExpiration));
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
        String first = jwtProvider.createRefreshToken(1L, Role.MEMBER, now);
        String second = jwtProvider.createRefreshToken(1L, Role.MEMBER, now);

        // then
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("refresh token에서 id와 Role을 읽습니다.")
    void parseRefreshToken() {
        // given
        String token = jwtProvider.createRefreshToken(1L, Role.ADMIN, Instant.now());

        // when & then
        assertThat(jwtParser.isValid(token)).isTrue();
        assertThat(jwtParser.getId(token)).isEqualTo(1L);
        assertThat(jwtParser.getRole(token)).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("refresh token은 access token으로, access token은 refresh token으로 쓸 수 없습니다.")
    void rejectTokenOfOtherType() {
        // given
        String accessToken = jwtProvider.createAccessToken(1L, Role.MEMBER, Instant.now());
        String refreshToken = jwtProvider.createRefreshToken(1L, Role.MEMBER, Instant.now());

        // when & then
        assertThat(jwtParser.isValid(accessToken)).isFalse();
        assertThatThrownBy(() -> jwtDecoder.decode(refreshToken)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("만료된 refresh token은 만료로 판단합니다.")
    void detectExpiredRefreshToken() {
        // given
        Instant issuedAt = Instant.now().minusSeconds(refreshTokenExpiration + 120);
        String token = jwtProvider.createRefreshToken(1L, Role.MEMBER, issuedAt);

        // when & then
        assertThat(jwtParser.isExpired(token)).isTrue();
        assertThat(jwtParser.isValid(token)).isFalse();
    }

    @Test
    @DisplayName("올바르지 않은 형식의 토큰은 유효하지도 만료되지도 않았고 값을 읽을 수 없습니다.")
    void rejectMalformedToken() {
        // given
        String token = "invalid token";

        // when & then
        assertThat(jwtParser.isValid(token)).isFalse();
        assertThat(jwtParser.isExpired(token)).isFalse();
        assertThatThrownBy(() -> jwtParser.getId(token)).isInstanceOf(JwtException.class);
    }
}
