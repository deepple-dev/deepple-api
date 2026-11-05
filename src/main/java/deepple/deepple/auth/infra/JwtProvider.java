package deepple.deepple.auth.infra;

import deepple.deepple.auth.domain.TokenProvider;
import deepple.deepple.common.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class JwtProvider implements TokenProvider {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(JwtEncoder jwtEncoder,
        @Value("${jwt.issuer}") String issuer,
        @Value("${jwt.access-token.expiration}") long accessTokenExpiration,
        @Value("${jwt.refresh-token.expiration}") long refreshTokenExpiration) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String createAccessToken(long id, Role role, Instant issuedAt) {
        return createToken(JwtSpec.ACCESS_TOKEN_TYPE, id, role, accessTokenExpiration, issuedAt);
    }

    public String createRefreshToken(long id, Role role, Instant issuedAt) {
        return createToken(JwtSpec.REFRESH_TOKEN_TYPE, id, role, refreshTokenExpiration, issuedAt);
    }

    private String createToken(String type, long id, Role role, long expiration, Instant issuedAt) {
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
            .type(type)
            .build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(issuer)
            .subject(String.valueOf(id))
            .audience(List.of(JwtSpec.AUDIENCE))
            .issuedAt(issuedAt)
            .expiresAt(issuedAt.plusSeconds(expiration))
            .id(UUID.randomUUID().toString())
            .claim(JwtSpec.ROLES_CLAIM, List.of(role.name()))
            .claim(JwtSpec.CLIENT_ID_CLAIM, JwtSpec.clientIdOf(role))
            .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims))
            .getTokenValue();
    }
}
